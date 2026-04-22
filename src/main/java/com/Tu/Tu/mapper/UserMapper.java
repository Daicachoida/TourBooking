package com.Tu.Tu.mapper;

import com.Tu.Tu.dto.request.UserCreateRequest;
import com.Tu.Tu.dto.response.UserResponse;
import com.Tu.Tu.entity.Role;
import com.Tu.Tu.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserCreateRequest request);

    UserResponse toResponse(User user);

    default String map(Role role){
        return role.getName();
    }
}
