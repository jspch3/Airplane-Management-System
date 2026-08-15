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

    public Flight registerFlight(Flight flight) {
        // Automatically populate carrierName from Carrier entity if blank or missing
        if (flight.getCarrierId() != null && (flight.getCarrierName() == null || flight.getCarrierName().trim().isEmpty())) {
            Carrier carrier = carrierRepository.findById(flight.getCarrierId()).orElse(null);
            if (carrier != null) {
                flight.setCarrierName(carrier.getCarrierName());
            } else {
                flight.setCarrierName("Carrier #" + flight.getCarrierId());
            }
        }

        // Enforce Business Fare > Economy Fare verification rule
        if (flight.getBusinessClassFare() != null && flight.getEconomyClassFare() != null) {
            if (flight.getBusinessClassFare() <= flight.getEconomyClassFare()) {
                throw new RuntimeException("Business Class Fare (₹" + flight.getBusinessClassFare() + 
                    ") must be greater than Economy Class Fare (₹" + flight.getEconomyClassFare() + ")");
            }
        }
        if (flight.getExecutiveClassFare() != null && flight.getBusinessClassFare() != null) {
            if (flight.getExecutiveClassFare() <= flight.getBusinessClassFare()) {
                throw new RuntimeException("Executive Class Fare (₹" + flight.getExecutiveClassFare() + 
                    ") must be greater than Business Class Fare (₹" + flight.getBusinessClassFare() + ")");
            }
        }
        if (flight.getOrigin() != null && flight.getOrigin().equalsIgnoreCase(flight.getDestination())) {
            throw new RuntimeException("Origin and Destination cities must be different.");
        }
        return flightRepository.save(flight);
    }

    public Flight updateFlight(Long flightId, Flight updatedFlight) {
        Flight existing = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found with ID: " + flightId));

        if (updatedFlight.getBusinessClassFare() != null && updatedFlight.getEconomyClassFare() != null) {
            if (updatedFlight.getBusinessClassFare() <= updatedFlight.getEconomyClassFare()) {
                throw new RuntimeException("Business Class Fare (₹" + updatedFlight.getBusinessClassFare() + 
                    ") must be greater than Economy Class Fare (₹" + updatedFlight.getEconomyClassFare() + ")");
            }
        }
        if (updatedFlight.getExecutiveClassFare() != null && updatedFlight.getBusinessClassFare() != null) {
            if (updatedFlight.getExecutiveClassFare() <= updatedFlight.getBusinessClassFare()) {
                throw new RuntimeException("Executive Class Fare (₹" + updatedFlight.getExecutiveClassFare() + 
                    ") must be greater than Business Class Fare (₹" + updatedFlight.getBusinessClassFare() + ")");
            }
        }
        if (updatedFlight.getOrigin() != null && updatedFlight.getOrigin().equalsIgnoreCase(updatedFlight.getDestination())) {
            throw new RuntimeException("Origin and Destination cities must be different.");
        }

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
