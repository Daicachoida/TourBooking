package com.Tu.Tu.configuration;

import com.Tu.Tu.constant.PredefinedPassengerType;
import com.Tu.Tu.constant.PredefinedRole;
import com.Tu.Tu.entity.PassengerType;
import com.Tu.Tu.entity.Role;
import com.Tu.Tu.entity.User;
import com.Tu.Tu.repository.PassengerTypeRepository;
import com.Tu.Tu.repository.RoleRepository;
import com.Tu.Tu.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin@gmail.com";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            name = "spring.datasource.driver-class-name",
            havingValue = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    )
    ApplicationRunner applicationRunner(UserRepository userRepository,
                                        RoleRepository roleRepository,
                                        PassengerTypeRepository passengerTypeRepository) {
        log.info("Initializing application........");
        return args -> {
            // Khởi tạo admin
            if (userRepository.findByEmail(ADMIN_USER_NAME).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(PredefinedRole.USER_ROLE)
                        .description("User role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name(PredefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());

                Role businessRole = roleRepository.save(Role.builder()
                        .name(PredefinedRole.BUSINESS_ROLE)
                        .description("Business role")
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);
                roles.add(businessRole);

                User user = User.builder()
                        .email(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            }

            // Khởi tạo passenger types
            if (passengerTypeRepository.count() == 0) {
                passengerTypeRepository.saveAll(List.of(
                        PassengerType.builder().code(PredefinedPassengerType.ADULT).name("Người lớn (>= 1.4m)").build(),
                        PassengerType.builder().code(PredefinedPassengerType.CHILD).name("Trẻ em (< 1.4m)").build(),
                        PassengerType.builder().code(PredefinedPassengerType.INFANT).name("Em bé (< 1 tuổi)").build()
                ));
                log.info("Passenger types initialized");
            }

            log.info("Application initialization completed .....");
        };
    }
}