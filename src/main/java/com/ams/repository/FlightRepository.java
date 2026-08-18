package com.ams.repository;

import com.ams.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByCarrierNameContainingIgnoreCase(String carrierName);
    List<Flight> findByOriginIgnoreCaseAndDestinationIgnoreCase(String origin, String destination);
    List<Flight> findByScheduleDate(LocalDate scheduleDate);
    List<Flight> findByCarrierNameContainingIgnoreCaseAndScheduleDate(String carrierName, LocalDate scheduleDate);
    boolean existsByCarrierId(Long carrierId);
}
