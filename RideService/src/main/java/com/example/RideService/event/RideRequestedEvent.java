package com.example.RideService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published to kafka when a ride is requested.
 * This event is consumed by the matching service to find a driver for the ride.
 */

@AllArgsConstructor
@NoArgsConstructor
@Data

public class RideRequestedEvent {

    private String rideId;
    private String riderId;
    private double pickupLatitude;
    private double pickupLongitude;

    private String pickupAddress;

    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;
}
