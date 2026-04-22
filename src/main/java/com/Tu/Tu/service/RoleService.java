package com.Tu.Tu.service;


import com.Tu.Tu.dto.request.RoleCreationRequest;
import com.Tu.Tu.dto.request.RoleUpdateRequest;
import com.Tu.Tu.dto.response.RoleResponse;
import com.Tu.Tu.entity.Role;
import com.Tu.Tu.exception.AppException;
import com.Tu.Tu.exception.ErrorCode;
import com.Tu.Tu.mapper.RoleMapper;
import com.Tu.Tu.repository.PermissionRepository;
import com.Tu.Tu.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class RoleService {
    private final RoleMapper roleMapper;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleResponse create(RoleCreationRequest request){
        Role role = roleMapper.toRole(request);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public RoleResponse update(RoleUpdateRequest request, String role){
        Role role1 = roleRepository.findById(role).orElseThrow(()->new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        roleMapper.update(role1, request);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role1.setPermissions(new HashSet<>(permissions));
        role1 = roleRepository.save(role1);
        return roleMapper.toRoleResponse(role1);
    }

    public List<RoleResponse> getAll(){
        List<Role> roleList = roleRepository.findAll();
        return roleList.stream().map(roleMapper::toRoleResponse).toList();
    }

    public void deleteRole(String role){
        roleRepository.deleteById(role);
    }

}
