package com.Tu.Tu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingUpdateRequest {
    @NotBlank(message = "Tên liên hệ không được để trống")
    String contactName;

    @NotBlank(message = "Email liên hệ không được để trống")
    String contactEmail;

    @Pattern(regexp = "^[0-9]{10}", message = "Số điện thoại phải đủ 10 chữ số")
    String contactPhone;

    String specialRequest;
}
