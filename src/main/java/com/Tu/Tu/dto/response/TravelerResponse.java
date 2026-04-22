package com.Tu.Tu.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TravelerResponse {
    Long id;
    String fullName;
    LocalDate dob;
    Long passengerTypeId;
    String passengerTypeCode;
    String passengerTypeName;
    Long price;
}
