package train.local.fogpass.service;

import org.springframework.data.domain.Pageable;
import train.local.fogpass.dto.masterdata.DivisionCreateRequest;
import train.local.fogpass.dto.masterdata.DivisionResponse;
import train.local.fogpass.dto.masterdata.DivisionUpdateRequest;
import train.local.fogpass.dto.response.PageResponse;

import java.util.List;

public interface DivisionService {
    
    DivisionResponse createDivision(DivisionCreateRequest request);
    
    DivisionResponse getDivisionById(Long id);
    
    List<DivisionResponse> getAllDivisions();
    
    PageResponse<DivisionResponse> getAllDivisions(Pageable pageable);
    
    List<DivisionResponse> getDivisionsByZone(Long zoneId);
    
    PageResponse<DivisionResponse> getDivisionsByZone(Long zoneId, Pageable pageable);
    
    PageResponse<DivisionResponse> searchDivisions(Long zoneId, String name, String code, Pageable pageable);
    
    DivisionResponse updateDivision(Long id, DivisionUpdateRequest request);
    
    void deleteDivision(Long id);
}