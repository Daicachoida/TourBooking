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
@Table(name = "departures")
public class Departure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDate departureDate;
    Integer capacity;
    Integer bookedSeats;
    String status;

    @ManyToOne
    @JoinColumn(name = "Tourid")
    Tour tour;

    @OneToMany(mappedBy = "departure", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Booking> bookingList;

    @OneToMany(mappedBy = "departure", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<DeparturePrice> departurePriceList;
}