package com.Tu.Tu.mapper;

import ch.qos.logback.core.model.ComponentModel;
import com.Tu.Tu.dto.response.DeparturePriceResponse;
import com.Tu.Tu.dto.response.DepartureResponse;
import com.Tu.Tu.entity.Departure;
import com.Tu.Tu.entity.DeparturePrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartureMapper {

    @Mapping(target = "tourId", source = "tour.id")
    @Mapping(target = "tourName", source = "tour.name")
    @Mapping(target = "availableSeats", expression = "java(departure.getCapacity() - departure.getBookedSeats())")
    DepartureResponse toResponse(Departure departure);

    @Mapping(target = "passengerTypeId", source = "passengerType.id")
    @Mapping(target = "passengerTypeCode", source = "passengerType.code")
    @Mapping(target = "passengerTypeName", source = "passengerType.name")
    DeparturePriceResponse toPriceResponse(DeparturePrice departurePrice);
}