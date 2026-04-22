package com.Tu.Tu.dto.request;

import com.Tu.Tu.validator.DobConstraint;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String fullName;

    @DobConstraint(min = 0, message = "Ngày sinh không hợp lệ")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dob;

    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải đủ 10 chữ số")
    String phone;
}
