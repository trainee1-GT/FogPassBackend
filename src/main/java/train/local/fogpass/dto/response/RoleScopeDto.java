package train.local.fogpass.dto.response;

public class RoleScopeDto {
    private String roleName;
    private Long zoneId;
    private Long divisionId;
    private Long sectionId;

    public RoleScopeDto() {}

    public RoleScopeDto(String roleName, Long zoneId, Long divisionId, Long sectionId) {
        this.roleName = roleName;
        this.zoneId = zoneId;
        this.divisionId = divisionId;
        this.sectionId = sectionId;
    }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public Long getDivisionId() { return divisionId; }
    public void setDivisionId(Long divisionId) { this.divisionId = divisionId; }
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
}