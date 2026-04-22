package com.Tu.Tu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long amount;
    String method;
    String status;
    LocalDate paymentTime;

    // VNPay fields
    String transactionRef;      // mã đối soát unique VD: "TU-1-1711234567"
    String vnpayTransactionId;  // mã GD VNPay trả về trong webhook
    LocalDateTime expiredAt;    // thời hạn thanh toán (tạo + 15 phút)

    @ManyToOne
    @JoinColumn(name = "Bookingid")
    Booking booking;
}
