package com.Tu.Tu.mapper;

import com.Tu.Tu.dto.request.TravelerUpdateRequest;
import com.Tu.Tu.dto.response.TravelerResponse;
import com.Tu.Tu.entity.Traveler;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TravelerMapper {
    @Mapping(target = "passengerTypeId", source = "passengerType.id")
    @Mapping(target = "passengerTypeCode", source = "passengerType.code")
    @Mapping(target = "passengerTypeName", source = "passengerType.name")
    @Mapping(target = "price", ignore = true)
    TravelerResponse toResponse(Traveler traveler);

    @Mapping(target = "passengerType", ignore = true)
    @Mapping(target = "booking", ignore = true)
    void updateTraveler(TravelerUpdateRequest request, @MappingTarget Traveler traveler);

}
