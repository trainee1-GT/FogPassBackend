package train.local.fogpass.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import train.local.fogpass.dto.masterdata.SectionCreateRequest;
import train.local.fogpass.dto.masterdata.SectionResponse;
import train.local.fogpass.dto.masterdata.SectionUpdateRequest;
import train.local.fogpass.entity.Section;
import train.local.fogpass.entity.enums.SectionStatus;

@Mapper(componentModel = "spring")
public interface SectionMapper {
    SectionMapper INSTANCE = Mappers.getMapper(SectionMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "division", ignore = true)
    @Mapping(target = "routes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedFields", ignore = true)
    Section toEntity(SectionCreateRequest dto);

    @Mapping(target = "divisionId", source = "division.id")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToString")
    SectionResponse toDto(Section entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "division", ignore = true)
    @Mapping(target = "routes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedFields", ignore = true)
    void updateEntityFromDto(SectionUpdateRequest dto, @MappingTarget Section entity);

    @Named("mapStatusToString")
    default String mapStatusToString(SectionStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("mapStringToStatus")
    default SectionStatus mapStringToStatus(String status) {
        return status != null ? SectionStatus.valueOf(status) : null;
    }
}
