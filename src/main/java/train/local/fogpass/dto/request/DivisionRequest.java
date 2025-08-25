package train.local.fogpass.dto.request;

public class DivisionRequest {
    private String name;
    private Long zoneId;

    public DivisionRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }
}