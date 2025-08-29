package train.local.fogpass.dto.request;

import java.util.List;

// For posting: one divisionId + list of section names
public class SectionBulkRequest {
    private Long divisionId;
    private List<String> sections; // names

    public Long getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Long divisionId) {
        this.divisionId = divisionId;
    }

    public List<String> getSections() {
        return sections;
    }

    public void setSections(List<String> sections) {
        this.sections = sections;
    }
}