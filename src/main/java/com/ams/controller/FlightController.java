package com.ams.controller;

import com.ams.domain.Flight;
import com.ams.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    // US005: Register Flight Details
    @PostMapping
    public ResponseEntity<Flight> registerFlight(@Valid @RequestBody Flight flight) {
        Flight savedFlight = flightService.registerFlight(flight);
        return new ResponseEntity<>(savedFlight, HttpStatus.CREATED);
    }

    // US006: View Flight Details / Search by Carrier
    @GetMapping
    public ResponseEntity<List<Flight>> viewFlights(@RequestParam(required = false) String carrierName) {
        if (carrierName != null && !carrierName.trim().isEmpty()) {
            return ResponseEntity.ok(flightService.getFlightsByCarrierName(carrierName));
        }
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    @GetMapping("/{flightId}")
    public ResponseEntity<Flight> getFlightById(@PathVariable Long flightId) {
        return flightService.getFlightById(flightId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // US007: Update Flight Details
    @PutMapping("/{flightId}")
    public ResponseEntity<Flight> updateFlightDetails(@PathVariable Long flightId, @Valid @RequestBody Flight flight) {
        Flight updatedFlight = flightService.updateFlight(flightId, flight);
        return ResponseEntity.ok(updatedFlight);
    }

    // Admin Delete Flight
    @DeleteMapping("/{flightId}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long flightId) {
        flightService.deleteFlight(flightId);
        return ResponseEntity.noContent().build();
    }
}
