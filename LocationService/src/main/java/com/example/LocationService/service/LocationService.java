package com.example.LocationService.service;

import com.example.LocationService.dto.DriverLocReq;
import com.example.LocationService.dto.NearByDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;

    //Redis key for all drover locations
    private static final String DRIVER_LOCATION_KEY = "drivers:locations";

    /**
     * update driver loc in Redis
     * called every 3 sec by driver
     * maps to redis GEOADD command
     */
    public void updateDriverLoc(DriverLocReq driverLocReq) {
        log.info("Updating loc for driver:{}", driverLocReq.getDriverId());

        // Geospatial std- 1st Longitude, 2nd Latitude
        Point driverPoint = new Point(
                driverLocReq.getLongitude(),
                driverLocReq.getLatitude()
        );

        //opsForGeo is a RedisTemplate operation for geospatial data
        redisTemplate.opsForGeo().add(
                DRIVER_LOCATION_KEY,
                driverPoint,
                driverLocReq.getDriverId()
        );
        log.info("Driver location updated: {}", driverLocReq.getDriverId());
    }

    /**
     * find nearby drivers within the given radius
     * called by matching service when a rider requests a ride
     * maps to redis GEORADIUS command
     */
    public List<NearByDriverResponse> findNearbyDrivers(
            double latitude, double longitude, double radiusInKm) {
        log.info("Finding nearby drivers for location: ({}, {}) within radius: {} km",
                latitude, longitude, radiusInKm);
        Circle searchArea = new Circle(new Point(longitude, latitude),
                new Distance(radiusInKm, Metrics.KILOMETERS));

    GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(
            DRIVER_LOCATION_KEY,
            searchArea,
            RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                    .includeCoordinates()
                    .includeDistance()
                    .sortAscending()
                    .limit(10)
    );

    List<NearByDriverResponse> nearbyDrivers = new ArrayList<>();
    if (results != null) {
        results.getContent().forEach(result -> {
            RedisGeoCommands.GeoLocation<String> location = result.getContent();
            nearbyDrivers.add(new NearByDriverResponse(
                    location.getName(),
                    location.getPoint().getY(), // Latitude
                    location.getPoint().getX(), // Longitude
                    result.getDistance().getValue()
            ));
        });
    }
    log.info("Found {} nearby drivers", nearbyDrivers.size());
    return nearbyDrivers;
    }

    /**
     * remove driver when they go offline
     * maps to redis ZREM command
     */
    public void removeDriver(String driverId){
        log.info("Removing driver: {} from Redis", driverId);
        redisTemplate.opsForGeo().remove(DRIVER_LOCATION_KEY, driverId);
        log.info("Driver removed: {}", driverId);
    }
}
