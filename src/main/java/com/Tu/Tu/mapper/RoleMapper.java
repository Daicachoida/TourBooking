package com.Tu.Tu.mapper;


import com.Tu.Tu.dto.request.RoleCreationRequest;
import com.Tu.Tu.dto.request.RoleUpdateRequest;
import com.Tu.Tu.dto.response.RoleResponse;
import com.Tu.Tu.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { PermissionMapper.class } )
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleCreationRequest request);

    RoleResponse toRoleResponse(Role role);

    @Mapping(target = "permissions", ignore = true)
    void update(@MappingTarget Role role, RoleUpdateRequest request);
}
