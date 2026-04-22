package com.Tu.Tu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TourUpdateRequest {
    @NotBlank(message = "Tên tour không được để trống")
    String name;

    String thumbnailUrl;
    List<String> imageUrls;
    String highlights;
    String servicesInfo;
    String description;
    @NotBlank(message = "Sdt liên lạc không được để trống")
    String contactPhone;

    @NotBlank(message = "Link facebook không được để trống")
    String facebookUrl;

    @Min(value = 1, message = "Số ngày phải lớn hơn 0")
    int durationDays;

    @NotBlank(message = "Điểm khởi hành không được để trống")
    String departureLocation;

    @Min(value = 0, message = "Giá không được âm")
    Long minPrice;
}