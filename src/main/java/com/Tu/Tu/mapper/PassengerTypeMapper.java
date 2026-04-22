package com.Tu.Tu.mapper;

import com.Tu.Tu.dto.response.PassengerTypeResponse;
import com.Tu.Tu.entity.PassengerType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PassengerTypeMapper {
    PassengerTypeResponse toResponse(PassengerType passengerType);
}