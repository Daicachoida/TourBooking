package com.Tu.Tu.scheduler;

import com.Tu.Tu.repository.DepartureRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepartureScheduler {

    private final DepartureRepository departureRepository;


    @Scheduled(cron = "0 5 0 * * *") // 00:05 mỗi ngày
    @Transactional
    public void markDepartedDepartures() {
        LocalDate today = LocalDate.now();
        int count = departureRepository.bulkMarkDeparted(today);

        if (count > 0) {
            log.info("DepartureScheduler: đã chuyển {} departure sang DEPARTED (ngày: {})", count, today);
        }
    }
}