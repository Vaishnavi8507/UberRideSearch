package com.example.RideService.service;

import com.example.RideService.event.RideMatchedEvent;
import com.example.RideService.model.RideStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideEventConsumer {

    private final RideService rideService;

    @KafkaListener(
            topics = "ride.matched",
            groupId = "ride-service-group"
    )
    public void consumeRideMatchedEvent(RideMatchedEvent event) {
        log.info("Consumed ride.matched event: {}", event);
        rideService.updateRideWithDriver(
                event.getRideId(),
                event.getDriverId()
        );
    }
}
