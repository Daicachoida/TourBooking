package com.Tu.Tu.dto.request;

import com.Tu.Tu.validator.DobConstraint;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.cglib.core.Local;
import org.springframework.web.bind.annotation.BindParam;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TravelerUpdateRequest {
    @NotBlank(message = "Họ tên không được để trống")
    String fullName;

    @DobConstraint(min = 0, message = "Ngày sinh không hợp lệ")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dob;
}
