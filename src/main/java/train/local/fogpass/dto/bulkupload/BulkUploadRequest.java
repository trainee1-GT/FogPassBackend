package train.local.fogpass.dto.bulkupload;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class BulkUploadRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotNull(message = "routeId is required")
    private Long routeId;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }
}