package com.Tu.Tu.dto.request;

import com.Tu.Tu.validator.DobConstraint;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class UserCreateRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password tối thiểu 6 ký tự")
    String password;

    @NotBlank(message = "Họ tên không được để trống")
    String fullName;

    @DobConstraint(min = 0, message = "Ngày sinh không hợp lệ")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dob;

    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải đủ 10 chữ số")
    String phone;
}
