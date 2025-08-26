package train.local.fogpass.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import train.local.fogpass.dto.masterdata.ZoneCreateRequest;
import train.local.fogpass.dto.masterdata.ZoneResponse;
import train.local.fogpass.dto.masterdata.ZoneUpdateRequest;
import train.local.fogpass.dto.response.PageResponse;
import train.local.fogpass.entity.Zone;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ZoneMapper {
    
    @Mapping(target = "divisionCount", expression = "java(zone.getDivisions() != null ? zone.getDivisions().size() : 0)")
    ZoneResponse toResponse(Zone zone);
    
    List<ZoneResponse> toResponseList(List<Zone> zones);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "divisions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Zone toEntity(ZoneCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "divisions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(ZoneUpdateRequest request, @MappingTarget Zone zone);
    
    default PageResponse<ZoneResponse> toPageResponse(Page<Zone> page) {
        PageResponse<ZoneResponse> response = new PageResponse<>();
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