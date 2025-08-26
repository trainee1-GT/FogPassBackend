package train.local.fogpass.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import train.local.fogpass.dto.masterdata.DivisionCreateRequest;
import train.local.fogpass.dto.masterdata.DivisionResponse;
import train.local.fogpass.dto.masterdata.DivisionUpdateRequest;
import train.local.fogpass.dto.response.PageResponse;
import train.local.fogpass.entity.Division;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DivisionMapper {
    
    @Mapping(target = "zoneId", source = "zone.id")
    @Mapping(target = "zoneName", source = "zone.name")
    @Mapping(target = "sectionCount", expression = "java(division.getSections() != null ? division.getSections().size() : 0)")
    DivisionResponse toResponse(Division division);
    
    List<DivisionResponse> toResponseList(List<Division> divisions);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Division toEntity(DivisionCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(DivisionUpdateRequest request, @MappingTarget Division division);
    
    default PageResponse<DivisionResponse> toPageResponse(Page<Division> page) {
        PageResponse<DivisionResponse> response = new PageResponse<>();
        response.setContent(toResponseList(page.getContent()));
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }
}