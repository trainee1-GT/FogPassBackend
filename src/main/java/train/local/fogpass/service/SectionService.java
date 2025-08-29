package train.local.fogpass.service;

import train.local.fogpass.dto.masterdata.SectionCreateRequest;
import train.local.fogpass.dto.masterdata.SectionResponse;
import train.local.fogpass.dto.masterdata.SectionUpdateRequest;

import java.util.List;

public interface SectionService {

    SectionResponse createSection(SectionCreateRequest request);

    SectionResponse updateSection(Long id, SectionUpdateRequest request);

    List<SectionResponse> getSectionsByDivision(Long divisionId);

    SectionResponse getSectionById(Long id);

    void deleteSection(Long id);
}
