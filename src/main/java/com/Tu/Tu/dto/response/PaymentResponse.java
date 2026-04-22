package com.Tu.Tu.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    Long id;
    Long amount;
    String method;
    String status;
    LocalDate paymentTime;

    String transactionRef;
    String vnpayTransactionId;
    LocalDateTime expiredAt;
    String paymentUrl;

    // Booking info
    Long bookingId;
    String bookingCode;

    // User info
    Long userId;
    String userName;
}