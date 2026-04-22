package com.Tu.Tu.service;


import com.Tu.Tu.constant.PredefinedRole;
import com.Tu.Tu.dto.request.AdminUserUpdateRequest;
import com.Tu.Tu.dto.request.ChangePasswordRequest;
import com.Tu.Tu.dto.request.UserCreateRequest;
import com.Tu.Tu.dto.request.UserUpdateRequest;
import com.Tu.Tu.dto.response.AdminUserResponse;
import com.Tu.Tu.dto.response.PageResponse;
import com.Tu.Tu.dto.response.UserResponse;
import com.Tu.Tu.entity.EmailVerificationToken;
import com.Tu.Tu.entity.Role;
import com.Tu.Tu.entity.User;
import com.Tu.Tu.exception.AppException;
import com.Tu.Tu.exception.ErrorCode;
import com.Tu.Tu.mapper.UserMapper;
import com.Tu.Tu.repository.EmailVerificationTokenRepository;
import com.Tu.Tu.repository.RoleRepository;
import com.Tu.Tu.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    EmailVerificationTokenRepository emailVerificationTokenRepository;
    EmailService emailService;


    public UserResponse createUser(UserCreateRequest request) {
        log.info("Service: create user");
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiryTime(java.time.LocalDateTime.now().plusHours(24))
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), token);
        return userMapper.toResponse(user);
    }

    public UserResponse getMyInfo() {
        log.info("Service: getMyInfo");
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    public UserResponse updateMyInfo(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setDob(request.getDob());

        return userMapper.toResponse(userRepository.save(user));
    }

    public void changePassword(ChangePasswordRequest request) {
        log.info("Service: changePassword");

        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setDob(request.getDob());

        return userMapper.toResponse(userRepository.save(user));
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public AdminUserResponse adminUpdateUser(Long id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getDob() != null) user.setDob(request.getDob());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getActive() != null) user.setActive(request.getActive());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleList() != null && !request.getRoleList().isEmpty()) {
            Set<Role> roles = request.getRoleList().stream()
                    .map(roleName -> roleRepository.findById(roleName)
                            .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)))
                    .collect(java.util.stream.Collectors.toSet());
            user.setRoles(roles);
        }

        User saved = userRepository.save(user);
        return AdminUserResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .dob(saved.getDob())
                .phone(saved.getPhone())
                .createAt(saved.getCreateAt())
                .active(saved.getActive())
                .roles(saved.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()))
                .build();
    }

    public PageResponse<UserResponse> getAllUser(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> result = userRepository.findAll(pageable);
        return toPageResponse(result);
    }

    public PageResponse<UserResponse> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> result = userRepository.search(normalizeKeyword(keyword), pageable);
        return toPageResponse(result);
    }

    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public UserResponse assignRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Role role = roleRepository.findById(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.getRoles().add(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    // ===== HELPER =====

    private PageResponse<UserResponse> toPageResponse(Page<User> page) {
        return PageResponse.<UserResponse>builder()
                .content(page.getContent().stream().map(userMapper::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN));

        if (verificationToken.getExpiryTime().isBefore(java.time.LocalDateTime.now()))
            throw new AppException(ErrorCode.VERIFICATION_TOKEN_EXPIRED);

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        emailVerificationTokenRepository.delete(verificationToken);
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getEmailVerified()))
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);

        // Xóa token cũ nếu có
        emailVerificationTokenRepository.deleteByUser_Id(user.getId());

        // Tạo token mới
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiryTime(java.time.LocalDateTime.now().plusHours(24))
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(email, token);
    }

    @Transactional
    public User findOrCreateGoogleUser(String email, String fullName) {
        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.setGoogleLinked(true);
                    existingUser.setEmailVerified(true);

                    if ((existingUser.getFullName() == null || existingUser.getFullName().isBlank())
                            && fullName != null && !fullName.isBlank()) {
                        existingUser.setFullName(fullName);
                    }

                    if (existingUser.getActive() == null) {
                        existingUser.setActive(true);
                    }

                    if (existingUser.getRoles() == null || existingUser.getRoles().isEmpty()) {
                        HashSet<Role> roles = new HashSet<>();
                        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
                        existingUser.setRoles(roles);
                    }

                    User savedUser = userRepository.save(existingUser);
                    savedUser.getRoles().size();
                    return savedUser;
                })
                .orElseGet(() -> {
                    HashSet<Role> roles = new HashSet<>();
                    roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

                    User newUser = User.builder()
                            .email(email)
                            .fullName((fullName == null || fullName.isBlank()) ? email : fullName)
                            .password(null)
                            .active(true)
                            .emailVerified(true)
                            .googleLinked(true)
                            .createAt(java.time.LocalDate.now())
                            .roles(roles)
                            .build();

                    User savedUser = userRepository.save(newUser);
                    savedUser.getRoles().size();
                    return savedUser;
                });
    }
}