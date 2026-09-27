package com.example.MatchingService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * event published to Kafka topic: ride.matched
 * Consumed by ride service to update ride with assigned driver
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RideRequestedEvent {
    private String riderId;
    private String rideId;
    private double pickupLatitude;
    private double pickupLongitude;
    private String pickupAddress;
    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;

    public double getPickUpLatitude() {
        return 0;
    }

    public double getPickupLatitude() {
        return  0;
    }
}
