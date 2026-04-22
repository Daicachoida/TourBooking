package com.Tu.Tu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "departurePrices")
public class DeparturePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PassengerTypeid")
    PassengerType passengerType;

    @ManyToOne
    @JoinColumn(name = "Departureid")
    Departure departure;
}