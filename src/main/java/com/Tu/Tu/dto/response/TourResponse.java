package com.Tu.Tu.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TourResponse {
    Long id;
    String code;
    String name;
    String thumbnailUrl;
    String highlights;
    String servicesInfo;
    String description;
    int durationDays;
    String departureLocation;
    Long minPrice;
    int reviewCount;
    Double averageRating;
    LocalDate createAt;
    Boolean isApproved;
    Boolean isVIPTour;
    String status;
    String contactPhone;
    String facebookUrl;
    Long userId;
    String ownerName;
    List<String> imageUrls;
    List<DepartureResponse> departures;
}