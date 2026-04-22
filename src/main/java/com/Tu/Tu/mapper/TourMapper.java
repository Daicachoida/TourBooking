package com.Tu.Tu.mapper;

import com.Tu.Tu.dto.request.TourCreateRequest;
import com.Tu.Tu.dto.request.TourUpdateRequest;
import com.Tu.Tu.dto.response.DeparturePriceResponse;
import com.Tu.Tu.dto.response.DepartureResponse;
import com.Tu.Tu.dto.response.TourResponse;
import com.Tu.Tu.entity.Departure;
import com.Tu.Tu.entity.DeparturePrice;
import com.Tu.Tu.entity.Tour;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TourMapper {

    @Mapping(target = "imageList", ignore = true)
    Tour toEntity(TourCreateRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "ownerName", source = "user.fullName")
    @Mapping(target = "isVIPTour", source = "isVIPTour")
    @Mapping(target = "imageUrls", expression = "java(tour.getImageList() == null ? java.util.Collections.emptyList() : tour.getImageList().stream().map(img -> img.getImage_url()).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "departures", expression = "java(mapDepartures(tour))")
    TourResponse toResponse(Tour tour);

    @Mapping(target = "imageList", ignore = true)
    void updateTour(TourUpdateRequest request, @MappingTarget Tour tour);

    default List<DepartureResponse> mapDepartures(Tour tour) {
        if (tour.getDepartureList() == null) return Collections.emptyList();
        return tour.getDepartureList().stream().map(this::mapDeparture).collect(Collectors.toList());
    }

    default DepartureResponse mapDeparture(Departure departure) {
        List<DeparturePriceResponse> prices = departure.getDeparturePriceList() == null
                ? Collections.emptyList()
                : departure.getDeparturePriceList().stream().map(this::mapPrice).collect(Collectors.toList());

        return DepartureResponse.builder()
                .id(departure.getId())
                .departureDate(departure.getDepartureDate())
                .capacity(departure.getCapacity())
                .bookedSeats(departure.getBookedSeats())
                .status(departure.getStatus())
                .prices(prices)
                .build();
    }

    default DeparturePriceResponse mapPrice(DeparturePrice dp) {
        return DeparturePriceResponse.builder()
                .id(dp.getId())
                .price(dp.getPrice())
                .passengerTypeCode(dp.getPassengerType() != null ? dp.getPassengerType().getCode() : null)
                .passengerTypeName(dp.getPassengerType() != null ? dp.getPassengerType().getName() : null)
                .build();
    }
}