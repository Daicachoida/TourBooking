package com.Tu.Tu.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tours")
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String code;

    @Nationalized
    @Column(columnDefinition = "NVARCHAR(255)")
    String name;
    String thumbnailUrl;

    @Nationalized
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String highlights;

    @Nationalized
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String servicesInfo;

    @Nationalized
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String description;

    int durationDays;
    @Nationalized
    @Column(columnDefinition = "NVARCHAR(255)")
    String departureLocation;
    Long minPrice;
    int reviewCount;
    Double averageRating;
    LocalDate createAt;
    Boolean isApproved;
    String status;
    String contactPhone;
    String facebookUrl;
    Boolean isVIPTour;

    @ManyToOne
    @JoinColumn(name = "Userid")
    User user;

    @OneToMany(mappedBy = "tour")
    List<Review> reviewList;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Image> imageList;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Departure> departureList;
}