package com.Tu.Tu.controller;

import com.Tu.Tu.dto.request.RoleCreationRequest;
import com.Tu.Tu.dto.request.RoleUpdateRequest;
import com.Tu.Tu.dto.response.ApiResponse;
import com.Tu.Tu.dto.response.RoleResponse;
import com.Tu.Tu.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/roles")
@RestController
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleCreationRequest request){
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll(){
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @PutMapping("/{role}")
    ApiResponse<RoleResponse> update(@PathVariable String role, @RequestBody RoleUpdateRequest request){
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.update(request, role))
                .build();
    }

    @DeleteMapping("/{role}")
    ApiResponse<Void> delete(@PathVariable String role){
        roleService.deleteRole(role);
        return ApiResponse.<Void>builder().build();
    }
}
