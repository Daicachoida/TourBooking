package com.Tu.Tu.controller;

import com.Tu.Tu.dto.response.ApiResponse;
import com.Tu.Tu.dto.response.PassengerTypeResponse;
import com.Tu.Tu.mapper.PassengerTypeMapper;
import com.Tu.Tu.repository.PassengerTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/passenger-types")
@RequiredArgsConstructor
public class PassengerTypeController {

    private final PassengerTypeRepository passengerTypeRepository;
    private final PassengerTypeMapper passengerTypeMapper;

    @GetMapping
    ApiResponse<List<PassengerTypeResponse>> getAll() {
        List<PassengerTypeResponse> result = passengerTypeRepository.findAll()
                .stream().map(passengerTypeMapper::toResponse).toList();
        return ApiResponse.<List<PassengerTypeResponse>>builder()
                .result(result)
                .message("success")
                .build();
    }
}