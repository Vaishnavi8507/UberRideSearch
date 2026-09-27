package com.example.RideService.repo;

import com.example.RideService.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepo extends JpaRepository<Ride, String> {
    List<Ride> findByRiderIdOrderByCreatedAtDesc(String riderId);
}
