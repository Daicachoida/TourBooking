package com.Tu.Tu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String bookingCode;
    String contactName;
    String contactEmail;
    String contactPhone;
    String specialRequest;
    Long totalAmount;
    LocalDate createAt;
    String status;

    @ManyToOne
    @JoinColumn(name = "Userid")
    User user;

    @ManyToOne
    @JoinColumn(name = "Departureid")
    Departure departure;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Payment> paymentList;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Traveler> travelerList;
}