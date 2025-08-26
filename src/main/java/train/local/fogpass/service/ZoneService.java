package train.local.fogpass.service;

import org.springframework.data.domain.Pageable;
import train.local.fogpass.dto.masterdata.ZoneCreateRequest;
import train.local.fogpass.dto.masterdata.ZoneResponse;
import train.local.fogpass.dto.masterdata.ZoneUpdateRequest;
import train.local.fogpass.dto.response.PageResponse;

import java.util.List;

public interface ZoneService {
    
    ZoneResponse createZone(ZoneCreateRequest request);
    
    ZoneResponse getZoneById(Long id);
    
    List<ZoneResponse> getAllZones();
    
    PageResponse<ZoneResponse> getAllZones(Pageable pageable);
    
    PageResponse<ZoneResponse> searchZones(String name, String code, Pageable pageable);
    
    ZoneResponse updateZone(Long id, ZoneUpdateRequest request);
    
    void deleteZone(Long id);
}