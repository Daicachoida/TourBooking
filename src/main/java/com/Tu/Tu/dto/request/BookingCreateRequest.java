package com.Tu.Tu.dto.request;

import com.Tu.Tu.dto.response.TravelerResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingCreateRequest {
    @NotNull(message = "Departure không được để trống")
    Long departureId;

    @NotBlank(message = "Email liên lạc không được để trống")
    String contactEmail;

    @NotBlank(message = "Tên liên lạc không được để trống")
    String contactName;

    String specialRequest;

    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải đủ 10 chữ số")
    String contactPhone;

    @NotEmpty(message = "Danh sách hành khách không được để trống")
    @Valid
    List<TravelerCreateRequest> travelerCreateRequestList;
}
