package com.Tu.Tu.mapper;


import com.Tu.Tu.dto.request.PermissionRequest;
import com.Tu.Tu.dto.response.PermissionResponse;
import com.Tu.Tu.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
