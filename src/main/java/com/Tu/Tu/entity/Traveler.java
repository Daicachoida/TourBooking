package com.Tu.Tu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "travelers")
public class Traveler {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String fullName;
    LocalDate dob;

    @ManyToOne
    @JoinColumn(name = "Bookingid")
    Booking booking;

    @ManyToOne
    @JoinColumn(name = "PassengerTypeid")
    PassengerType passengerType;
}
