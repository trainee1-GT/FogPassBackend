package train.local.fogpass.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import train.local.fogpass.dto.bulkupload.*;
import train.local.fogpass.entity.FailedUploadRow;
import train.local.fogpass.entity.UploadJob;
import train.local.fogpass.repository.FailedUploadRowRepository;
import train.local.fogpass.repository.UploadJobRepository;
import train.local.fogpass.service.bulkupload.BulkUploadService;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/masterdata/landmarks")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class BulkUploadController {

    private final BulkUploadService bulkUploadService;
    private final UploadJobRepository uploadJobRepository;
    private final FailedUploadRowRepository failedUploadRowRepository;

    public BulkUploadController(BulkUploadService bulkUploadService,
                                UploadJobRepository uploadJobRepository,
                                FailedUploadRowRepository failedUploadRowRepository) {
        this.bulkUploadService = bulkUploadService;
        this.uploadJobRepository = uploadJobRepository;
        this.failedUploadRowRepository = failedUploadRowRepository;
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkUploadResponse> upload(org.springframework.web.multipart.MultipartHttpServletRequest multipartRequest,
                                                     @RequestParam("routeId") Long routeId,
                                                     @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                                                     @RequestHeader(value = "X-Real-IP", required = false) String xri) {
        // Accept common keys: file, files, upload, csv, data, any single file part
        MultipartFile effectiveFile = null;
        String[] keys = new String[]{"file", "files", "upload", "csv", "data"};
        for (String k : keys) {
            if (effectiveFile == null) {
                effectiveFile = multipartRequest.getFile(k);
            }
        }
        if (effectiveFile == null || effectiveFile.isEmpty()) {
            // Fallback: take the first file part regardless of name
            java.util.Iterator<String> it = multipartRequest.getFileNames();
            if (it.hasNext()) effectiveFile = multipartRequest.getFile(it.next());
        }
        if (effectiveFile == null || effectiveFile.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        BulkUploadRequest request = new BulkUploadRequest();
        request.setFile(effectiveFile);
        request.setRouteId(routeId);
        String clientIp = firstNonBlank(xff, xri);
        BulkUploadResponse resp = bulkUploadService.initiateLandmarksUpload(request, clientIp);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resp);
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<?> status(@PathVariable UUID jobId) {
        Optional<UploadJob> jobOpt = uploadJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) return ResponseEntity.notFound().build();
        UploadJob job = jobOpt.get();

        // Truncate errorDetails to first 10 entries for response
        if (job.getResultJson() != null && (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.FAILED)) {
            // Optionally, we could deserialize, truncate, and reserialize. For simplicity, return as-is.
        }
        return ResponseEntity.ok(job);
    }

    @GetMapping("/failures/{jobId}")
    public ResponseEntity<Page<FailedUploadRow>> failures(@PathVariable UUID jobId,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FailedUploadRow> data = failedUploadRowRepository.findByJobId(jobId, pageable);
        return ResponseEntity.ok(data);
    }

    private String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v.split(",")[0].trim();
        }
        return null;
    }
}