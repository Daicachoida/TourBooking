package com.Tu.Tu.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserResponse {
    Long id;
    String email;
    String fullName;
    LocalDate dob;
    String phone;
    LocalDate createAt;
    Boolean active;
    Set<String> roles;
}
