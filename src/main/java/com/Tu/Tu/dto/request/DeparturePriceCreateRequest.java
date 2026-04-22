package com.Tu.Tu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeparturePriceCreateRequest {
    @NotNull
    Long departureId;

    @NotNull
    Long passengerTypeId;

    @NotNull
    @Min(value = 0, message = "Giá không được âm")
    Long price;
}
