package com.ams.repository;

import com.ams.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByCarrierNameContainingIgnoreCase(String carrierName);
    List<Flight> findByOriginIgnoreCaseAndDestinationIgnoreCase(String origin, String destination);
}
