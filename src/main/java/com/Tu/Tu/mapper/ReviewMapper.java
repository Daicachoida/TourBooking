package com.Tu.Tu.mapper;

import com.Tu.Tu.dto.request.ReviewCreateRequest;
import com.Tu.Tu.dto.request.ReviewUpdateRequest;
import com.Tu.Tu.dto.response.ReviewResponse;
import com.Tu.Tu.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "tourId", source = "tour.id")
    @Mapping(target = "tourName", source = "tour.name")
    ReviewResponse toResponse(Review review);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tour", ignore = true)
    Review toEntity(ReviewCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tour", ignore = true)
    void updateReview(ReviewUpdateRequest request, @MappingTarget Review review);
}