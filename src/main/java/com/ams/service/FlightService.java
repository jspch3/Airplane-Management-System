package com.ams.service;

import com.ams.domain.Carrier;
import com.ams.domain.Flight;
import com.ams.repository.CarrierRepository;
import com.ams.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final CarrierRepository carrierRepository;

    private void validateFlightRules(Flight flight) {
        if (flight.getOrigin() != null && flight.getOrigin().equalsIgnoreCase(flight.getDestination())) {
            throw new IllegalArgumentException("Origin and Destination cities must be different.");
        }

        // Fares Hierarchy Validation
        if (flight.getBusinessClassFare() != null && flight.getEconomyClassFare() != null) {
            if (flight.getBusinessClassFare() <= flight.getEconomyClassFare()) {
                throw new IllegalArgumentException("Business Class Fare (₹" + flight.getBusinessClassFare() + 
                    ") must be greater than Economy Class Fare (₹" + flight.getEconomyClassFare() + ")");
            }
        }
        if (flight.getExecutiveClassFare() != null && flight.getBusinessClassFare() != null) {
            if (flight.getExecutiveClassFare() <= flight.getBusinessClassFare()) {
                throw new IllegalArgumentException("Executive Class Fare (₹" + flight.getExecutiveClassFare() + 
                    ") must be greater than Business Class Fare (₹" + flight.getBusinessClassFare() + ")");
            }
        }

        // Seat Capacity Boundaries Validation [1, 1000]
        Integer eco = flight.getSeatCapacityEconomyClass();
        Integer bus = flight.getSeatCapacityBusinessClass();
        Integer exe = flight.getSeatCapacityExecutiveClass();

        if (eco != null && (eco < 1 || eco > 1000)) {
            throw new IllegalArgumentException("Economy seat capacity must be strictly between 1 and 1000.");
        }
        if (bus != null && (bus < 1 || bus > 1000)) {
            throw new IllegalArgumentException("Business seat capacity must be strictly between 1 and 1000.");
        }
        if (exe != null && (exe < 1 || exe > 1000)) {
            throw new IllegalArgumentException("Executive seat capacity must be strictly between 1 and 1000.");
        }

        // Capacity Hierarchy Validation: Economy Seats > Business Seats > Executive Seats
        if (eco != null && bus != null && eco <= bus) {
            throw new IllegalArgumentException("Economy seat capacity (" + eco + ") must be greater than Business seat capacity (" + bus + ").");
        }
        if (bus != null && exe != null && bus <= exe) {
            throw new IllegalArgumentException("Business seat capacity (" + bus + ") must be greater than Executive seat capacity (" + exe + ").");
        }
    }

    public Flight registerFlight(Flight flight) {
        if (flight.getCarrierId() != null && (flight.getCarrierName() == null || flight.getCarrierName().trim().isEmpty())) {
            Carrier carrier = carrierRepository.findById(flight.getCarrierId()).orElse(null);
            if (carrier != null) {
                flight.setCarrierName(carrier.getCarrierName());
            } else {
                flight.setCarrierName("Carrier #" + flight.getCarrierId());
            }
        }

        validateFlightRules(flight);
        return flightRepository.save(flight);
    }

    public Flight updateFlight(Long flightId, Flight updatedFlight) {
        Flight existing = flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found with ID: " + flightId));

        validateFlightRules(updatedFlight);

        String carrierName = updatedFlight.getCarrierName();
        if (updatedFlight.getCarrierId() != null && (carrierName == null || carrierName.trim().isEmpty())) {
            Carrier carrier = carrierRepository.findById(updatedFlight.getCarrierId()).orElse(null);
            if (carrier != null) {
                carrierName = carrier.getCarrierName();
            }
        }

        existing.setCarrierId(updatedFlight.getCarrierId());
        if (carrierName != null && !carrierName.trim().isEmpty()) {
            existing.setCarrierName(carrierName);
        }
        existing.setOrigin(updatedFlight.getOrigin());
        existing.setDestination(updatedFlight.getDestination());
        existing.setDepartureTime(updatedFlight.getDepartureTime());
        existing.setArrivalTime(updatedFlight.getArrivalTime());
        existing.setAirFare(updatedFlight.getAirFare());
        existing.setEconomyClassFare(updatedFlight.getEconomyClassFare());
        existing.setBusinessClassFare(updatedFlight.getBusinessClassFare());
        existing.setExecutiveClassFare(updatedFlight.getExecutiveClassFare());
        existing.setSeatCapacityEconomyClass(updatedFlight.getSeatCapacityEconomyClass());
        existing.setSeatCapacityBusinessClass(updatedFlight.getSeatCapacityBusinessClass());
        existing.setSeatCapacityExecutiveClass(updatedFlight.getSeatCapacityExecutiveClass());

        return flightRepository.save(existing);
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public Optional<Flight> getFlightById(Long id) {
        return flightRepository.findById(id);
    }

    public List<Flight> searchByCarrierName(String carrierName) {
        return flightRepository.findByCarrierNameContainingIgnoreCase(carrierName);
    }

    public List<Flight> getFlightsByCarrierName(String carrierName) {
        return searchByCarrierName(carrierName);
    }

    public void deleteFlight(Long id) {
        flightRepository.deleteById(id);
    }
}
