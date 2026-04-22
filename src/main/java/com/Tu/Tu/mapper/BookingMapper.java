package com.Tu.Tu.mapper;

import com.Tu.Tu.dto.request.BookingUpdateRequest;
import com.Tu.Tu.dto.response.BookingResponse;
import com.Tu.Tu.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "departureId", source = "departure.id")
    @Mapping(target = "tourName", source = "departure.tour.name")
    @Mapping(target = "departureDate", source = "departure.departureDate")
    @Mapping(target = "travelerResponseList", ignore = true)
    BookingResponse toResponse(Booking booking);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookingCode", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "departure", ignore = true)
    @Mapping(target = "travelerList", ignore = true)
    @Mapping(target = "paymentList", ignore = true)
    void updateBooking(BookingUpdateRequest request, @MappingTarget Booking booking);
}
