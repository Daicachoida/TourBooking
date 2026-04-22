package com.Tu.Tu.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeparturePriceResponse {
    Long id;
    Long price;
    Long passengerTypeId;
    String passengerTypeCode;  // ADULT, CHILD, INFANT
    String passengerTypeName;  // Người lớn, Trẻ em, Em bé
}