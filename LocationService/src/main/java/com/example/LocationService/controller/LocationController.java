package com.example.LocationService.controller;

import com.example.LocationService.dto.DriverLocReq;
import com.example.LocationService.dto.NearByDriverResponse;
import com.example.LocationService.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/drivers/update")
    public ResponseEntity<String> updateDriverLocation(@RequestBody DriverLocReq driverLocReq) {
        locationService.updateDriverLoc(driverLocReq);
        return ResponseEntity.ok("Driver location updated successfully");
    }

    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearByDriverResponse>> getNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam (defaultValue = "5.0") double radius) {
        return ResponseEntity.ok(locationService.findNearbyDrivers(latitude, longitude, radius));
    }

    @DeleteMapping("/drivers/{driverId}")
    public ResponseEntity<String> removeDriver(@PathVariable String driverId) {
        locationService.removeDriver(driverId);
        return ResponseEntity.ok("Driver removed successfully");
    }
}
