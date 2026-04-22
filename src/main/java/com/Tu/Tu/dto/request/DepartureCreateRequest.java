package com.Tu.Tu.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class DepartureCreateRequest {
    @NotNull
    Long tourId;

    @NotNull
    @Future(message = "Ngày khởi hành phải ở tương lai")
    LocalDate departureDate;

    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    Integer capacity;
}
