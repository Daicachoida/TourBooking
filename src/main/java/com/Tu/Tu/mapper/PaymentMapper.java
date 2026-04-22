package com.Tu.Tu.mapper;

import com.Tu.Tu.dto.response.PaymentResponse;
import com.Tu.Tu.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "bookingCode", source = "booking.bookingCode")
    @Mapping(target = "userId", source = "booking.user.id")
    @Mapping(target = "userName", source = "booking.user.fullName")
    @Mapping(target = "paymentUrl", ignore = true)
    PaymentResponse toResponse(Payment payment);
}
