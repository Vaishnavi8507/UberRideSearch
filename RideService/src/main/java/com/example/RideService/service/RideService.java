package com.example.RideService.service;

import com.example.RideService.dto.RideRequest;
import com.example.RideService.dto.RideResponse;
import com.example.RideService.event.RideRequestedEvent;
import com.example.RideService.model.Ride;
import com.example.RideService.model.RideStatus;
import com.example.RideService.repo.RideRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {

    private final RideRepo rideRepo;
    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;
    private static final String RIDE_REQUESTED_TOPIC = "ride.requested";

    /**
     * create a new ride in DB with requested status
     */
    public RideResponse requestRide(RideRequest rideRequest) {
        log.info("Requesting ride from driver: {}", rideRequest.getRiderId());

        // Save the ride request to the database
        Ride ride = new Ride();
        ride.setRiderId(rideRequest.getRiderId());
        ride.setPickupLatitude(rideRequest.getPickupLatitude());
        ride.setPickupLongitude(rideRequest.getPickupLongitude());
        ride.setPickupAddress(rideRequest.getPickupAddress());
        ride.setDropLatitude(rideRequest.getDropLatitude());
        ride.setDropLongitude(rideRequest.getDropLongitude());
        ride.setDropAddress(rideRequest.getDropAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimatedFare(rideRequest));

        Ride savedRide = rideRepo.save(ride);

        //Publish the event to Kafka - matching services will consume this & find nearest driver
        RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getId(),
                savedRide.getRiderId(),
                savedRide.getPickupLatitude(),
                savedRide.getPickupLongitude(),
                savedRide.getPickupAddress(),
                savedRide.getDropLatitude(),
                savedRide.getDropLongitude(),
                savedRide.getDropAddress()
        );
        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, savedRide.getId(), event);
        log.info("RideRequestedEvent published to kafka for ride: {}", savedRide.getId());

        //update to matching status
        savedRide.setStatus(RideStatus.MATCHING);
        rideRepo.save(savedRide);

        return mapToResponse(savedRide);
    }

    public void updateRideWithDriver(String rideId, String driverId) {
        log.info("Updating ride {} with driver {}", rideId, driverId);
        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        rideRepo.save(ride);
    }

    public RideResponse startRide(String rideId) {
        log.info("Starting ride {}", rideId);
        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));
        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(LocalDateTime.now());
        rideRepo.save(ride);
        return mapToResponse(ride);
    }

    public RideResponse completeRide(String rideId) {
        log.info("Completing ride {}", rideId);
        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));
        ride.setStatus(RideStatus.COMPLETED);
        ride.setActualFare(ride.getEstimatedFare());
        ride.setCompletedAt(LocalDateTime.now());
        rideRepo.save(ride);
        return mapToResponse(ride);
    }

    public RideResponse cancelRide(String rideId) {
        log.info("Cancelling ride {}", rideId);
        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));
        ride.setStatus(RideStatus.CANCELLED);
        rideRepo.save(ride);
        return mapToResponse(ride);
    }

    public RideResponse getRideById(String rideId) {
        log.info("Fetching ride {}", rideId);
        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));
        return mapToResponse(ride);
    }

    public List<RideResponse> getRidesByRider(String riderId) {
        return rideRepo.findByRiderIdOrderByCreatedAtDesc(riderId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    private double calculateEstimatedFare(RideRequest request) {
        double lat1 = Math.toRadians(request.getPickupLatitude());
        double lat2 = Math.toRadians(request.getDropLatitude());

        double lon1 = Math.toRadians(request.getPickupLongitude());
        double lon2 = Math.toRadians(request.getDropLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));
        double distanceKm = 6371 * c; // Radius of Earth in kilometers

        //12 Rs per km + 50 Rs base fare
        double fare = 50 + (distanceKm * 12);
        return Math.round(fare * 100.0) / 100.0; // Round to 2 decimal places
    }

    private RideResponse mapToResponse(Ride ride) {
        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setRiderId(ride.getRiderId());
        response.setDriverId(ride.getDriverId());
        response.setPickupLatitude(ride.getPickupLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());
        response.setPickupAddress(ride.getPickupAddress());
        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());
        response.setDropAddress(ride.getDropAddress());
        response.setStatus(ride.getStatus());
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());
        response.setCreatedAt(ride.getCreatedAt());
        response.setUpdatedAt(ride.getUpdatedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());
        return response;
    }

}
