package com.Tu.Tu.dto.request;

import com.Tu.Tu.validator.DobConstraint;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TravelerCreateRequest {
    @NotBlank(message = "Họ tên không được để trống")
    String fullName;

    @DobConstraint(min = 0, message = "Ngày sinh không hợp lệ")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dob;

    @NotNull(message = "Loại hành khách không được để trống")
    Long passengerTypeId;
}
