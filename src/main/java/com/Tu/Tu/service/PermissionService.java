package com.Tu.Tu.service;


import com.Tu.Tu.dto.request.PermissionRequest;
import com.Tu.Tu.dto.response.PermissionResponse;
import com.Tu.Tu.entity.Permission;
import com.Tu.Tu.mapper.PermissionMapper;
import com.Tu.Tu.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class PermissionService {
    private final PermissionMapper permissionMapper;
    private final PermissionRepository permissionRepository;

    public PermissionResponse create(PermissionRequest request){
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);

        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll(){
        var permissionList = permissionRepository.findAll();
        return permissionList.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void deletePermission(String permission){
        permissionRepository.deleteById(permission);
    }
}
