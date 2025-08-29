package train.local.fogpass.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import train.local.fogpass.dto.masterdata.RoleCreateRequest;
import train.local.fogpass.dto.masterdata.RoleResponse;
import train.local.fogpass.dto.masterdata.RoleUpdateRequest;
import train.local.fogpass.entity.Role;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {

    // DTO -> Entity
    Role toEntity(RoleCreateRequest request);

    // Entity -> DTO
    RoleResponse toResponse(Role role);

    // Partial update
    void updateEntity(@MappingTarget Role role, RoleUpdateRequest request);
}