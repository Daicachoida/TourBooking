package com.Tu.Tu.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    Long id;
    String bookingCode;
    String contactName;
    String contactPhone;
    String contactEmail;
    String specialRequest;
    Long totalAmount;
    LocalDate createAt;
    String status;

    Long userId;
    String userName;

    Long departureId;
    LocalDate departureDate;
    String tourName;

    List<TravelerResponse> travelerResponseList;

}
