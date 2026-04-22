package com.Tu.Tu.controller;

import com.Tu.Tu.dto.request.AdminUserUpdateRequest;
import com.Tu.Tu.dto.request.ChangePasswordRequest;
import com.Tu.Tu.dto.request.UserCreateRequest;
import com.Tu.Tu.dto.request.UserUpdateRequest;
import com.Tu.Tu.dto.response.AdminUserResponse;
import com.Tu.Tu.dto.response.ApiResponse;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.dto.response.UserResponse;
import com.Tu.Tu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        log.info("Create user");
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .message("success")
                .build();
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> getMyInfo() {
        log.info("get MyInfo.");
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .message("success")
                .build();
    }

    @PutMapping("/me")
    ApiResponse<UserResponse> updateMyInfo(@RequestBody UserUpdateRequest request) {
        log.info("UserController: update my info");
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateMyInfo(request))
                .message("success")
                .build();
    }

    @PutMapping("/password")
    ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        log.info("UserController: change password");
        userService.changePassword(request);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get all user.");
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(userService.getAllUser(page, size))
                .message("success")
                .build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<PageResponse<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(userService.searchUsers(keyword, page, size))
                .message("success")
                .build();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<UserResponse> getUserById(@PathVariable Long userId) {
        log.info("Get user by id: " + userId);
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(userId))
                .message("success")
                .build();
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<AdminUserResponse> adminUpdateUser(@PathVariable Long userId,
                                                   @RequestBody @Valid AdminUserUpdateRequest request) {
        log.info("UserController: admin update user");
        return ApiResponse.<AdminUserResponse>builder()
                .result(userService.adminUpdateUser(userId, request))
                .message("success")
                .build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<Void> deleteUserById(@PathVariable Long userId) {
        log.info("UserController: delete user by id: " + userId);
        userService.deleteUser(userId);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{userId}/assign-role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    ApiResponse<UserResponse> assignRole(@PathVariable Long userId,
                                         @RequestParam String roleName) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.assignRole(userId, roleName))
                .build();
    }
}