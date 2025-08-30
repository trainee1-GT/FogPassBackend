package train.local.fogpass.mapper;

import org.mapstruct.*;
import train.local.fogpass.dto.masterdata.RouteCreateRequest;
import train.local.fogpass.dto.masterdata.RouteResponse;
import train.local.fogpass.dto.masterdata.RouteUpdateRequest;
import train.local.fogpass.entity.Route;
import train.local.fogpass.entity.enums.RouteStatus;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "landmarks", ignore = true)
    @Mapping(target = "journeys", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Route toEntity(RouteCreateRequest dto);

    @Mapping(source = "section.id", target = "sectionId")
    @Mapping(target = "status", expression = "java(mapStatusToString(entity.getStatus()))")
    RouteResponse toDto(Route entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "routeCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "landmarks", ignore = true)
    @Mapping(target = "journeys", ignore = true)
    void updateEntityFromDto(RouteUpdateRequest dto, @MappingTarget Route entity);

    default String mapStatusToString(RouteStatus status) {
        return status != null ? status.name() : null;
    }
}