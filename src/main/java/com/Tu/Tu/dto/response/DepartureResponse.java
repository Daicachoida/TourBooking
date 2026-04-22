package com.Tu.Tu.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepartureResponse {
    Long id;
    LocalDate departureDate;
    Integer capacity;
    Integer bookedSeats;
    Integer availableSeats;  // capacity - bookedSeats, tính sẵn cho FE
    String status;
    Long tourId;
    String tourName;
    List<DeparturePriceResponse> prices;
}