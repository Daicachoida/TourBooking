package com.Tu.Tu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "passengerTypes")
public class PassengerType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String code;

    String name;

    @OneToMany(mappedBy = "passengerType")
    List<Traveler> travelerList;

    @OneToMany(mappedBy = "passengerType")
    List<DeparturePrice> departurePriceList;
}