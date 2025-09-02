package train.local.fogpass.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "failed_upload_rows", indexes = {
        @Index(name = "idx_failed_rows_job_id", columnList = "job_id")
})
public class FailedUploadRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID jobId;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Lob
    @Column(name = "row_data")
    private String rowData;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
    public String getRowData() { return rowData; }
    public void setRowData(String rowData) { this.rowData = rowData; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}