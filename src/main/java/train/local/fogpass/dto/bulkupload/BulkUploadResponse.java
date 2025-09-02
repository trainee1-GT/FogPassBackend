package train.local.fogpass.dto.bulkupload;

import java.util.UUID;

public class BulkUploadResponse {

    private UUID jobId;
    private JobStatus status;
    private String message;

    public BulkUploadResponse() {}

    public BulkUploadResponse(UUID jobId, JobStatus status, String message) {
        this.jobId = jobId;
        this.status = status;
        this.message = message;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}