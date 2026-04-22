package com.Tu.Tu.entity;

import com.Tu.Tu.entity.User;
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
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    int rating;
    String comment;
    LocalDate createAt;

    @ManyToOne
    @JoinColumn(name = "Userid")
    User user;

    @ManyToOne
    @JoinColumn(name = "Tourid")
    Tour tour;
}
