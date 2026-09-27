package com.example.RideService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideMatchedEvent {
    private String rideId;
    private String driverId;
    private double riderId;
    private double driverLatitude;
    private double driverLongitude;
    private double distanceToPickup;
}
