package train.local.fogpass.service.bulkupload.impl;

import com.opencsv.CSVReader;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import train.local.fogpass.audit.AuditEvents;
import train.local.fogpass.dto.bulkupload.BulkUploadRequest;
import train.local.fogpass.dto.bulkupload.BulkUploadResponse;
import train.local.fogpass.dto.bulkupload.JobStatus;
import train.local.fogpass.entity.FileAsset;
import train.local.fogpass.entity.FailedUploadRow;
import train.local.fogpass.repository.FailedUploadRowRepository;
import train.local.fogpass.repository.FileAssetRepository;
import train.local.fogpass.repository.UploadJobRepository;
import train.local.fogpass.service.FileStorageService;
import train.local.fogpass.service.bulkupload.BulkUploadService;
import train.local.fogpass.exception.DuplicateResourceException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class BulkUploadServiceImpl implements BulkUploadService {

    private final FileStorageService storage;
    private final FileAssetRepository fileAssetRepo;
    private final UploadJobRepository uploadJobRepo;
    private final FailedUploadRowRepository failedRowRepo;
    private final ThreadPoolTaskExecutor executor;
    private final Counter jobsStarted;
    private final Counter jobsFailed;
    private final Counter rowsProcessed;
    private final Timer duration;
    private final ApplicationEventPublisher events;

    public BulkUploadServiceImpl(FileStorageService storage,
                                 FileAssetRepository fileAssetRepo,
                                 UploadJobRepository uploadJobRepo,
                                 FailedUploadRowRepository failedRowRepo,
                                 ThreadPoolTaskExecutor bulkUploadExecutor,
                                 MeterRegistry registry,
                                 ApplicationEventPublisher events) {
        this.storage = storage;
        this.fileAssetRepo = fileAssetRepo;
        this.uploadJobRepo = uploadJobRepo;
        this.failedRowRepo = failedRowRepo;
        this.executor = bulkUploadExecutor;
        this.jobsStarted = registry.counter("bulk_upload_jobs_started");
        this.jobsFailed = registry.counter("bulk_upload_jobs_failed");
        this.rowsProcessed = registry.counter("bulk_upload_total_rows_processed");
        this.duration = registry.timer("bulk_upload_duration_seconds");
        this.events = events;
    }

    @Override
    @Transactional
    public BulkUploadResponse initiateLandmarksUpload(BulkUploadRequest request, String clientIp) {
        var file = request.getFile();

        // Security scope check (plug into your real security logic)
        checkSecurityScope(request.getRouteId());

        String checksum = computeSha256(file);
        fileAssetRepo.findByChecksumSha256(checksum).ifPresent(existing -> {
            throw new DuplicateResourceException("Duplicate file (checksum exists): " + checksum);
        });

        String storedPath;
        try {
            String storageName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            storedPath = storage.storeFile(file, request.getRouteId(), storageName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e);
        }

        FileAsset asset = new FileAsset();
        asset.setAssetType(FileAsset.AssetType.LANDMARKS_CSV);
        asset.setOriginalFileName(file.getOriginalFilename());
        asset.setStoragePath(storedPath);
        asset.setFileType(file.getContentType());
        asset.setSizeBytes(file.getSize());
        asset.setChecksumSha256(checksum);
        asset.setStatus(FileAsset.AssetStatus.UPLOADED);
        asset = fileAssetRepo.save(asset);

        var job = new train.local.fogpass.entity.UploadJob();
        job.setJobId(UUID.randomUUID());
        job.setFileName(file.getOriginalFilename());
        job.setStatus(JobStatus.PENDING);
        job.setUploadTimestamp(Instant.now());
        job.setClientIp(clientIp);
        job.setFileAsset(asset);
        uploadJobRepo.save(job);

        jobsStarted.increment();

        UUID jobId = job.getJobId();
        Long routeId = request.getRouteId();
        Path path = Path.of(storedPath);
        executor.submit(() -> processFileInBackground(jobId, path, routeId));

        return new BulkUploadResponse(jobId, JobStatus.PENDING, "Bulk upload job accepted and is being processed");
    }

    // Example placeholder; integrate with your security service
    private void checkSecurityScope(Long routeId) {
        // securityService.assertCanUploadForRoute(routeId);
    }

    private String computeSha256(org.springframework.web.multipart.MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) md.update(buf, 0, r);
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new RuntimeException("Checksum failure", e);
        }
    }

    private static Integer tryParseInt(String s) {
        try { return s == null ? null : Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }
    private static String safeTrim(String s) { return s == null ? null : s.trim(); }

    @Override
    public void processFileInBackground(UUID jobId, Path filePath, Long routeId) {
        duration.record(() -> {
            try {
                var job = uploadJobRepo.findById(jobId).orElseThrow();
                var asset = job.getFileAsset();
                job.setStatus(JobStatus.PROCESSING);
                asset.setStatus(FileAsset.AssetStatus.PROCESSING);
                uploadJobRepo.save(job);
                fileAssetRepo.save(asset);

                int processed = 0;
                int schemaErrors = 0, domainErrors = 0, dupErrors = 0, ioErrors = 0;

                try (var reader = new CSVReader(new InputStreamReader(Files.newInputStream(filePath)))) {
                    String[] row;
                    int rowNum = 0;
                    java.util.Set<String> rowKeys = new java.util.HashSet<>();
                    while ((row = reader.readNext()) != null) {
                        rowNum++;
                        try {
                            // Minimal schema check
                            if (row.length < 3) {
                                persistFailed(jobId, rowNum, row, FailedUploadRow.ErrorCategory.SCHEMA_ERROR, "Insufficient columns");
                                schemaErrors++; continue;
                            }
                            // Map / validate
                            Integer seq = tryParseInt(row[0]);
                            String loc = safeTrim(row[1]);
                            String name = safeTrim(row[2]);

                            if (seq == null || seq < 0) {
                                persistFailed(jobId, rowNum, row, FailedUploadRow.ErrorCategory.DOMAIN_ERROR, "Invalid sequenceOrder");
                                domainErrors++; continue;
                            }
                            if (loc == null || loc.isEmpty()) {
                                persistFailed(jobId, rowNum, row, FailedUploadRow.ErrorCategory.DOMAIN_ERROR, "locationCode required");
                                domainErrors++; continue;
                            }

                            // Duplicate inside file: sequenceOrder + locationCode
                            String key = seq + "::" + loc;
                            if (!rowKeys.add(key)) {
                                persistFailed(jobId, rowNum, row, FailedUploadRow.ErrorCategory.DUPLICATE_ERROR, "Duplicate row in file (sequenceOrder + locationCode)");
                                dupErrors++; continue;
                            }

                            // TODO: Persist Landmark mapped from row
                            processed++;
                        } catch (Exception ex) {
                            persistFailed(jobId, rowNum, row, FailedUploadRow.ErrorCategory.IO_ERROR, ex.getMessage());
                            ioErrors++;
                        }
                        rowsProcessed.increment();
                    }
                }

                String result = """
                {"processed":%d,"schemaErrors":%d,"domainErrors":%d,"duplicateErrors":%d,"ioErrors":%d}
                """.formatted(processed, schemaErrors, domainErrors, dupErrors, ioErrors);

                job.setResultJson(result);
                job.setStatus(JobStatus.COMPLETED);
                asset.setErrorDetails(result);
                // move file to processed canonical zones/... path
                try {
                    String finalName = "landmarks_" + routeId + ".csv";
                    String newPath = storage.moveToProcessedForRoute(filePath.toString(), routeId, finalName);
                    asset.setStoragePath(newPath);
                } catch (Exception moveEx) {
                    // keep old path but mark error details; job still completes
                    asset.setErrorDetails(result + " | moveError=" + moveEx.getMessage());
                }
                asset.setStatus(FileAsset.AssetStatus.PROCESSED);
                uploadJobRepo.save(job);
                fileAssetRepo.save(asset);

                // Publish audit event
                events.publishEvent(new AuditEvents.BulkUploadCompleted(this, jobId, java.util.Map.of(
                        "processed", processed,
                        "schemaErrors", schemaErrors,
                        "domainErrors", domainErrors,
                        "duplicateErrors", dupErrors,
                        "ioErrors", ioErrors,
                        "assetId", asset.getId(),
                        "assetType", asset.getAssetType().name(),
                        "status", "COMPLETED"
                )));

            } catch (Exception e) {
                jobsFailed.increment();
                try {
                    var job = uploadJobRepo.findById(jobId).orElse(null);
                    if (job != null) {
                        job.setStatus(JobStatus.FAILED);
                        uploadJobRepo.save(job);
                        var asset = job.getFileAsset();
                        if (asset != null) {
                            asset.setStatus(FileAsset.AssetStatus.FAILED);
                            asset.setErrorDetails("{\"error\":\"" + e.getMessage().replace("\"","'") + "\"}");
                            fileAssetRepo.save(asset);
                        }
                    }
                    // Publish audit event
                    events.publishEvent(new AuditEvents.BulkUploadCompleted(this, jobId, java.util.Map.of(
                            "status", "FAILED",
                            "message", String.valueOf(e.getMessage())
                    )));
                } catch (Exception ignore) {}
            }
        });
    }

    private void persistFailed(UUID jobId, int rowNum, String[] row, FailedUploadRow.ErrorCategory cat, String message) {
        FailedUploadRow f = new FailedUploadRow();
        f.setJobId(jobId);
        f.setRowNumber(rowNum);
        f.setRowData(String.join(",", row));
        f.setErrorMessage(message);
        f.setErrorCategory(cat);
        failedRowRepo.save(f);
    }
}