package train.local.fogpass.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import train.local.fogpass.dto.bulkupload.JobStatus;
import train.local.fogpass.entity.LandmarkFile;

@Entity
@Table(name = "upload_jobs", indexes = {
        @Index(name = "idx_upload_jobs_status", columnList = "status"),
        @Index(name = "idx_upload_jobs_timestamp", columnList = "upload_timestamp")
})
public class UploadJob {

    @Id
    @Column(name = "job_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID jobId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobStatus status;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column(name = "upload_timestamp", nullable = false)
    private Instant uploadTimestamp;

    @Column(name = "result_json", columnDefinition = "json")
    private String resultJson;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "landmark_file_id", referencedColumnName = "id")
    private LandmarkFile landmarkFile;

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public Instant getUploadTimestamp() { return uploadTimestamp; }
    public void setUploadTimestamp(Instant uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public LandmarkFile getLandmarkFile() { return landmarkFile; }
    public void setLandmarkFile(LandmarkFile landmarkFile) { this.landmarkFile = landmarkFile; }
}