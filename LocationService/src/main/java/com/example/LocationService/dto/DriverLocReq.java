package com.example.LocationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverLocReq {
    private String driverId;
    private double latitude;
    private double longitude;
}
