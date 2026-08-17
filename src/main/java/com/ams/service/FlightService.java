package com.ams.service;

import com.ams.domain.Carrier;
import com.ams.domain.Flight;
import com.ams.repository.BookingRepository;
import com.ams.repository.CarrierRepository;
import com.ams.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final CarrierRepository carrierRepository;
    private final BookingRepository bookingRepository;

    private static final Map<String, Integer> ROUTE_DURATION_MINUTES = new HashMap<>();

    static {
        // Distance-based flight durations in minutes
        ROUTE_DURATION_MINUTES.put("MUMBAI-DELHI", 135);
        ROUTE_DURATION_MINUTES.put("DELHI-MUMBAI", 135);
        ROUTE_DURATION_MINUTES.put("MUMBAI-BENGALURU", 105);
        ROUTE_DURATION_MINUTES.put("BENGALURU-MUMBAI", 105);
        ROUTE_DURATION_MINUTES.put("MUMBAI-HYDERABAD", 85);
        ROUTE_DURATION_MINUTES.put("HYDERABAD-MUMBAI", 85);
        ROUTE_DURATION_MINUTES.put("MUMBAI-CHENNAI", 125);
        ROUTE_DURATION_MINUTES.put("CHENNAI-MUMBAI", 125);
        ROUTE_DURATION_MINUTES.put("MUMBAI-KOLKATA", 165);
        ROUTE_DURATION_MINUTES.put("KOLKATA-MUMBAI", 165);
        ROUTE_DURATION_MINUTES.put("MUMBAI-DUBAI", 210);
        ROUTE_DURATION_MINUTES.put("DUBAI-MUMBAI", 210);
        ROUTE_DURATION_MINUTES.put("DELHI-BENGALURU", 170);
        ROUTE_DURATION_MINUTES.put("BENGALURU-DELHI", 170);
        ROUTE_DURATION_MINUTES.put("DELHI-CHENNAI", 175);
        ROUTE_DURATION_MINUTES.put("CHENNAI-DELHI", 175);
        ROUTE_DURATION_MINUTES.put("DELHI-LONDON", 555);
        ROUTE_DURATION_MINUTES.put("LONDON-DELHI", 555);
        ROUTE_DURATION_MINUTES.put("DELHI-NEW YORK", 930);
        ROUTE_DURATION_MINUTES.put("NEW YORK-DELHI", 930);
        ROUTE_DURATION_MINUTES.put("VIJAYAWADA-VISAKHAPATNAM", 60);
        ROUTE_DURATION_MINUTES.put("VISAKHAPATNAM-VIJAYAWADA", 60);
    }

    private String cleanCityName(String city) {
        if (city == null) return "";
        return city.replaceAll("\\s*\\([^)]*\\)", "").trim().toUpperCase();
    }

    public int calculateFlightDurationMinutes(String origin, String destination) {
        String key = cleanCityName(origin) + "-" + cleanCityName(destination);
        if (ROUTE_DURATION_MINUTES.containsKey(key)) {
            return ROUTE_DURATION_MINUTES.get(key);
        }
        return 120; // Default 2 hours for standard domestic routes
    }

    public String computeArrivalTime(String origin, String destination, String departureTimeStr) {
        if (departureTimeStr == null || departureTimeStr.isBlank()) {
            return "12:00 PM";
        }

        try {
            String trimmed = departureTimeStr.trim();
            LocalTime depTime;

            if (trimmed.toUpperCase().endsWith("AM") || trimmed.toUpperCase().endsWith("PM")) {
                depTime = LocalTime.parse(trimmed.toUpperCase(), DateTimeFormatter.ofPattern("h:mm a"));
            } else {
                depTime = LocalTime.parse(trimmed, DateTimeFormatter.ofPattern("H:mm"));
            }

            int durationMins = calculateFlightDurationMinutes(origin, destination);
            LocalTime arrTime = depTime.plusMinutes(durationMins);

            return arrTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
        } catch (DateTimeParseException e) {
            return departureTimeStr + " + 2h";
        }
    }

    private void validateFlightRules(Flight flight) {
        if (flight.getOrigin() != null && flight.getOrigin().equalsIgnoreCase(flight.getDestination())) {
            throw new IllegalArgumentException("Origin and Destination cities must be different.");
        }

        // Schedule Date & Departure Time Validation
        if (flight.getScheduleDate() != null) {
            LocalDate today = LocalDate.now();
            if (flight.getScheduleDate().isBefore(today)) {
                throw new IllegalArgumentException("Flight schedule date cannot be in the past.");
            }

            if (flight.getScheduleDate().isEqual(today) && flight.getDepartureTime() != null) {
                try {
                    String trimmed = flight.getDepartureTime().trim();
                    LocalTime depTime;
                    if (trimmed.toUpperCase().endsWith("AM") || trimmed.toUpperCase().endsWith("PM")) {
                        depTime = LocalTime.parse(trimmed.toUpperCase(), DateTimeFormatter.ofPattern("h:mm a"));
                    } else {
                        depTime = LocalTime.parse(trimmed, DateTimeFormatter.ofPattern("H:mm"));
                    }

                    if (LocalTime.now().isAfter(depTime)) {
                        throw new IllegalArgumentException("Departure time cannot be earlier than the current time for today's flight schedule.");
                    }
                } catch (DateTimeParseException e) {
                    // Ignore parse errors here if non-standard string
                }
            }
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

    @Transactional
    public Flight registerFlight(Flight flight) {
        if (flight.getScheduleDate() == null) {
            flight.setScheduleDate(LocalDate.now());
        }

        if (flight.getCarrierId() != null && (flight.getCarrierName() == null || flight.getCarrierName().trim().isEmpty())) {
            Carrier carrier = carrierRepository.findById(flight.getCarrierId()).orElse(null);
            if (carrier != null) {
                flight.setCarrierName(carrier.getCarrierName());
            } else {
                flight.setCarrierName("Carrier #" + flight.getCarrierId());
            }
        }

        // Compute distance-based arrival time if missing/default
        if (flight.getOrigin() != null && flight.getDestination() != null && flight.getDepartureTime() != null) {
            flight.setArrivalTime(computeArrivalTime(flight.getOrigin(), flight.getDestination(), flight.getDepartureTime()));
        }

        validateFlightRules(flight);
        return flightRepository.save(flight);
    }

    @Transactional
    public Flight updateFlight(Long flightId, Flight updatedFlight) {
        Flight existing = flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found with ID: " + flightId));

        if (updatedFlight.getScheduleDate() == null) {
            updatedFlight.setScheduleDate(existing.getScheduleDate() != null ? existing.getScheduleDate() : LocalDate.now());
        }

        // Auto-calculate distance arrival time
        if (updatedFlight.getOrigin() != null && updatedFlight.getDestination() != null && updatedFlight.getDepartureTime() != null) {
            updatedFlight.setArrivalTime(computeArrivalTime(updatedFlight.getOrigin(), updatedFlight.getDestination(), updatedFlight.getDepartureTime()));
        }

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
        existing.setScheduleDate(updatedFlight.getScheduleDate());
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

    public List<Flight> getFlightsByScheduleDate(LocalDate scheduleDate) {
        return flightRepository.findByScheduleDate(scheduleDate);
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

    @Transactional
    public void deleteFlight(Long id) {
        if (!flightRepository.existsById(id)) {
            throw new IllegalArgumentException("Flight not found with ID: " + id);
        }

        // Active booking check before flight deletion
        boolean hasActiveBookings = bookingRepository.existsByFlightIdAndBookingStatusNot(id, "CANCELLED");
        if (hasActiveBookings) {
            throw new IllegalArgumentException("We can't delete the flight, it has active users.");
        }

        flightRepository.deleteById(id);
    }
}
