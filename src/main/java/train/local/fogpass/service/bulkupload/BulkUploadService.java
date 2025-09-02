package train.local.fogpass.service.bulkupload;

import java.nio.file.Path;
import java.util.UUID;
import train.local.fogpass.dto.bulkupload.BulkUploadRequest;
import train.local.fogpass.dto.bulkupload.BulkUploadResponse;

public interface BulkUploadService {
    BulkUploadResponse initiateLandmarksUpload(BulkUploadRequest request, String clientIp);
    void processFileInBackground(UUID jobId, Path filePath, Long routeId);
}