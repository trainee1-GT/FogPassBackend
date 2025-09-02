package train.local.fogpass.dto.bulkupload;

import java.util.ArrayList;
import java.util.List;

public class BulkUploadResult {
    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<String> errorDetails = new ArrayList<>();

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public List<String> getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(List<String> errorDetails) {
        this.errorDetails = errorDetails;
    }

    public List<String> firstTenErrors() {
        return errorDetails.size() <= 10 ? errorDetails : errorDetails.subList(0, 10);
    }
}