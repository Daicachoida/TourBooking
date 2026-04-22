package com.Tu.Tu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeparturePriceUpdateRequest {

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá không được âm")
    Long price;
}