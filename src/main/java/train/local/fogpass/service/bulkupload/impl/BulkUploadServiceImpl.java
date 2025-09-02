package train.local.fogpass.service.bulkupload.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import train.local.fogpass.dto.bulkupload.*;
import train.local.fogpass.entity.FailedUploadRow;
import train.local.fogpass.entity.Landmark;
import train.local.fogpass.entity.LandmarkFile;
import train.local.fogpass.entity.Route;
import train.local.fogpass.entity.UploadJob;
import train.local.fogpass.repository.*;
import train.local.fogpass.service.bulkupload.BulkUploadService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class BulkUploadServiceImpl implements BulkUploadService {

    private final UploadJobRepository uploadJobRepository;
    private final FailedUploadRowRepository failedUploadRowRepository;
    private final RouteRepository routeRepository;
    private final LandmarkRepository landmarkRepository;
    private final ObjectMapper objectMapper;
    private final train.local.fogpass.service.LandmarkFileService landmarkFileService;

    private final Counter jobsStarted;
    private final Counter jobsCompleted;
    private final Counter jobsFailed;
    private final Timer jobDuration;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${bulk-upload.max-rows:10000}")
    private int maxRows;

    public BulkUploadServiceImpl(UploadJobRepository uploadJobRepository,
                                 FailedUploadRowRepository failedUploadRowRepository,
                                 RouteRepository routeRepository,
                                 LandmarkRepository landmarkRepository,
                                 ObjectMapper objectMapper,
                                 MeterRegistry meterRegistry,
                                 train.local.fogpass.service.LandmarkFileService landmarkFileService) {
        this.uploadJobRepository = uploadJobRepository;
        this.failedUploadRowRepository = failedUploadRowRepository;
        this.routeRepository = routeRepository;
        this.landmarkRepository = landmarkRepository;
        this.objectMapper = objectMapper;
        this.jobsStarted = meterRegistry.counter("bulk_upload_jobs_started");
        this.jobsCompleted = meterRegistry.counter("bulk_upload_jobs_completed");
        this.jobsFailed = meterRegistry.counter("bulk_upload_jobs_failed");
        this.jobDuration = meterRegistry.timer("bulk_upload_duration_seconds");
        this.landmarkFileService = landmarkFileService;
    }

    @Override
    @Transactional
    public BulkUploadResponse initiateLandmarksUpload(BulkUploadRequest request, String clientIp) {
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        Long routeId = request.getRouteId();
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NoSuchElementException("Route not found: " + routeId));

        String filename = Objects.requireNonNull(file.getOriginalFilename(), "filename");
        String ext = getExtension(filename);
        if (!List.of("csv", "xls", "xlsx").contains(ext)) {
            throw new IllegalArgumentException("Unsupported file type: " + ext);
        }

        // throttle by processing jobs
        long processing = uploadJobRepository.countByStatus(JobStatus.PROCESSING);
        if (processing >= Runtime.getRuntime().availableProcessors()) {
            // Optional backpressure behavior
        }

        UUID jobId = UUID.randomUUID();
        UploadJob job = new UploadJob();
        job.setJobId(jobId);
        job.setFileName(filename);
        job.setStatus(JobStatus.PENDING);
        job.setUploadTimestamp(Instant.now());
        job.setUploadedBy(resolveCurrentUsername());
        job.setClientIp(clientIp);
        uploadJobRepository.save(job);

        // Save a tracked copy of the file via LandmarkFileService and link to the UploadJob
        try {
            LandmarkFile landmarkFile = landmarkFileService.uploadFile(file, route.getId());
            job.setLandmarkFile(landmarkFile);
            uploadJobRepository.save(job); // Update job with landmark file reference
        } catch (Exception e) {
            log.error("Failed to save landmark file record for job {}", jobId, e);
            // Continue with the bulk upload even if file tracking fails
        }

        // Save temp file to disk for processing (this temp copy is deleted after processing)
        try {
            Path dir = Path.of(uploadDir);
            Files.createDirectories(dir);
            Path dest = dir.resolve(jobId + "_" + sanitize(filename));
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            jobsStarted.increment();
            processFileInBackground(jobId, dest, route.getId());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        return new BulkUploadResponse(jobId, JobStatus.PENDING, "Upload accepted");
    }

    @Override
    @Async("bulkUploadTaskExecutor")
    @Transactional
    public void processFileInBackground(UUID jobId, Path filePath, Long routeId) {
        long startNanos = System.nanoTime();
        BulkUploadResult result = new BulkUploadResult();
        try {
            UploadJob job = uploadJobRepository.findById(jobId).orElseThrow();
            job.setStatus(JobStatus.PROCESSING);
            uploadJobRepository.save(job);

            String ext = getExtension(filePath.getFileName().toString());
            if ("csv".equalsIgnoreCase(ext)) {
                processCsv(filePath, routeId, result, jobId);
            } else {
                processExcel(filePath, routeId, result, jobId);
            }

            result.setTotalRows(result.getSuccessCount() + result.getFailureCount());
            job.setStatus(JobStatus.COMPLETED);
            job.setResultJson(objectToJson(result));
            uploadJobRepository.save(job);
            jobsCompleted.increment();
        } catch (Exception e) {
            UploadJob job = uploadJobRepository.findById(jobId).orElse(null);
            if (job != null) {
                job.setStatus(JobStatus.FAILED);
                if (job.getResultJson() == null) {
                    result.getErrorDetails().add("Fatal error: " + e.getMessage());
                    job.setResultJson(objectToJson(result));
                }
                uploadJobRepository.save(job);
            }
            jobsFailed.increment();
        } finally {
            try { Files.deleteIfExists(filePath); } catch (IOException ignored) {}
            long duration = System.nanoTime() - startNanos;
            jobDuration.record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    private void processCsv(Path filePath, Long routeId, BulkUploadResult result, UUID jobId) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String firstLine;
            int lineNo = 0;
            List<Landmark> batch = new ArrayList<>(1000);
            Route route = routeRepository.findById(routeId).orElseThrow();

            // Read first non-blank line to detect delimiter and header
            do { firstLine = reader.readLine(); if (firstLine == null) return; lineNo++; } while (firstLine.isBlank());
            String delimiter = firstLine.contains("\t") ? "\t" : ",";
            String lower = firstLine.toLowerCase(Locale.ROOT);
            boolean looksLikeHeader = lower.contains("location") && lower.contains("latitude");

            int seq = 0;
            if (!looksLikeHeader) {
                // Process the first line as data
                try {
                    seq++;
                    Landmark lm = parseDelimitedRow(firstLine, route, delimiter, seq);
                    validateLandmark(lm);
                    batch.add(lm);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (Exception ex) {
                    result.setFailureCount(result.getFailureCount() + 1);
                    String message = "Row " + lineNo + ": " + ex.getMessage();
                    result.getErrorDetails().add(message);
                    persistFailure(jobId, lineNo, firstLine, ex.getMessage());
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) continue;
                try {
                    seq++;
                    Landmark lm = parseDelimitedRow(line, route, delimiter, seq);
                    validateLandmark(lm);
                    batch.add(lm);
                    if (batch.size() >= 1000) {
                        landmarkRepository.saveAll(batch);
                        batch.clear();
                    }
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (Exception ex) {
                    result.setFailureCount(result.getFailureCount() + 1);
                    String message = "Row " + lineNo + ": " + ex.getMessage();
                    result.getErrorDetails().add(message);
                    persistFailure(jobId, lineNo, line, ex.getMessage());
                }
                if (result.getSuccessCount() + result.getFailureCount() >= maxRows) break;
            }
            if (!batch.isEmpty()) {
                landmarkRepository.saveAll(batch);
            }
        }
    }

    private void processExcel(Path filePath, Long routeId, BulkUploadResult result, UUID jobId) throws Exception {
        try (OPCPackage pkg = OPCPackage.open(filePath.toFile());
             XSSFWorkbook xssf = new XSSFWorkbook(pkg);
             SXSSFWorkbook wb = new SXSSFWorkbook(xssf, 100)) {
            Sheet sheet = wb.getXSSFWorkbook().getSheetAt(0);
            int lineNo = 0;
            List<Landmark> batch = new ArrayList<>(1000);
            Route route = routeRepository.findById(routeId).orElseThrow();

            for (Row row : sheet) {
                lineNo++;
                if (lineNo == 1) {
                    // assume first row header, skip
                    continue;
                }
                try {
                    Landmark lm = parseExcelRow(row, route);
                    validateLandmark(lm);
                    batch.add(lm);
                    if (batch.size() >= 1000) {
                        landmarkRepository.saveAll(batch);
                        batch.clear();
                    }
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (Exception ex) {
                    result.setFailureCount(result.getFailureCount() + 1);
                    String message = "Row " + lineNo + ": " + ex.getMessage();
                    result.getErrorDetails().add(message);
                    persistFailure(jobId, lineNo, compactExcelRow(row), ex.getMessage());
                }
                if (result.getSuccessCount() + result.getFailureCount() >= maxRows) break;
            }
            if (!batch.isEmpty()) {
                landmarkRepository.saveAll(batch);
            }
        }
    }

    private Landmark parseCsvRow(String line, Route route) {
        // Deprecated in favor of parseDelimitedRow
        return parseDelimitedRow(line, route, ",", null);
    }

    private Landmark parseDelimitedRow(String line, Route route, String delimiter, Integer sequenceOverride) {
        // Expected header order from sample:
        // Location Code | Landmark Type | Landmark Name | Latitude | Longitude | Prewarning Distance | Status
        String[] rawParts = line.split(delimiter, -1);
        String[] parts = Arrays.stream(rawParts).map(String::trim).toArray(String[]::new);
        if (parts.length < 7) {
            throw new IllegalArgumentException("Expected 7 columns, got " + parts.length);
        }
        Landmark lm = new Landmark();
        lm.setRoute(route);
        // If sequence is not present in file, use incremental value provided by caller
        lm.setSequenceOrder(sequenceOverride);
        lm.setLocationCode(parts[0]);
        lm.setLandmarkType(parts[1]);
        lm.setName(parts[2]);
        // Convert coordinates if provided in ddmm.mmmm format (e.g., 2526.7599 => 25 + 26.7599/60)
        lm.setLatitude(parseGeo(parts[3], "latitude"));
        lm.setLongitude(parseGeo(parts[4], "longitude"));
        lm.setPrewarningDistance(parseInteger(parts[5], "prewarningDistance"));
        lm.setDirection(parts[6]); // using Status column as direction per sample (e.g., UP/DOWN)
        return lm;
    }

    private BigDecimal parseGeo(String s, String field) {
        String v = s == null ? null : s.trim();
        if (v == null || v.isEmpty()) return null;
        // Try decimal first
        try {
            BigDecimal bd = new BigDecimal(v);
            // If clearly out of range (> 180), attempt ddmm.mmmm conversion
            if (bd.abs().compareTo(BigDecimal.valueOf(180)) <= 0) {
                return bd; // already decimal degrees
            }
        } catch (NumberFormatException ignore) {}
        // ddmm.mmmm format handling
        try {
            double val = Double.parseDouble(v);
            int dd = (int)(val / 100); // integer degrees
            double mm = val - dd * 100; // minutes with fraction
            double dec = dd + (mm / 60.0);
            return BigDecimal.valueOf(dec);
        } catch (Exception e) {
            throw new IllegalArgumentException(field + " must be decimal degrees or ddmm.mmmm");
        }
    }

    private Landmark parseExcelRow(Row row, Route route) {
        Landmark lm = new Landmark();
        lm.setRoute(route);
        lm.setSequenceOrder((int) numeric(row.getCell(0), "sequenceOrder"));
        lm.setLocationCode(string(row.getCell(1)));
        lm.setLandmarkType(string(row.getCell(2)));
        lm.setName(string(row.getCell(3)));
        lm.setLatitude(new BigDecimal(string(row.getCell(4))));
        lm.setLongitude(new BigDecimal(string(row.getCell(5))));
        lm.setPrewarningDistance((int) numeric(row.getCell(6), "prewarningDistance"));
        lm.setDirection(string(row.getCell(7)));
        return lm;
    }

    private void validateLandmark(Landmark lm) {
        if (lm.getLatitude() == null || lm.getLongitude() == null) {
            throw new IllegalArgumentException("Latitude/Longitude required");
        }
        BigDecimal lat = lm.getLatitude();
        BigDecimal lon = lm.getLongitude();
        if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new IllegalArgumentException("invalid latitude '" + lat + "'");
        }
        if (lon.compareTo(BigDecimal.valueOf(-180)) < 0 || lon.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("invalid longitude '" + lon + "'");
        }
        if (lm.getSequenceOrder() == null || lm.getSequenceOrder() < 0) {
            throw new IllegalArgumentException("sequenceOrder must be >= 0");
        }
        if (lm.getPrewarningDistance() != null && lm.getPrewarningDistance() < 0) {
            throw new IllegalArgumentException("prewarningDistance must be >= 0");
        }
    }

    private void persistFailure(UUID jobId, int rowNumber, String rowData, String error) {
        FailedUploadRow fail = new FailedUploadRow();
        fail.setJobId(jobId);
        fail.setRowNumber(rowNumber);
        fail.setRowData(rowData);
        fail.setErrorMessage(error);
        failedUploadRowRepository.save(fail);
    }

    private String compactExcelRow(Row row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            Cell c = row.getCell(i);
            sb.append(c == null ? "" : c.toString());
            if (i < 7) sb.append(',');
        }
        return sb.toString();
    }

    private String objectToJson(Object o) {
        try { return objectMapper.writeValueAsString(o); } catch (Exception e) { return "{}"; }
    }

    private String getExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx > 0 ? name.substring(idx + 1).toLowerCase(Locale.ROOT) : "";
    }

    private String sanitize(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String string(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
        String s = cell.getStringCellValue();
        return s != null ? s.trim() : null;
    }

    private double numeric(Cell cell, String field) {
        if (cell == null) throw new IllegalArgumentException(field + " required");
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> Double.parseDouble(cell.getStringCellValue());
            default -> throw new IllegalArgumentException(field + " must be numeric");
        };
    }

    private Integer parseInteger(String s, String field) {
        String v = s == null ? null : s.trim();
        if (v == null || v.isEmpty()) return null;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { throw new IllegalArgumentException(field + " must be integer"); }
    }

    private BigDecimal parseDecimal(String s, String field) {
        String v = s == null ? null : s.trim();
        if (v == null || v.isEmpty()) return null;
        try { return new BigDecimal(v); } catch (NumberFormatException e) { throw new IllegalArgumentException(field + " must be decimal"); }
    }

    private String resolveCurrentUsername() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "system";
        } catch (Exception e) { return "system"; }
    }
}