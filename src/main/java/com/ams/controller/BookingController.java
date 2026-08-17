package com.ams.controller;

import com.ams.domain.Booking;
import com.ams.dto.BookingRequestDTO;
import com.ams.dto.PartialCancelRequestDTO;
import com.ams.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // US009: Book Flight Details
    @PostMapping
    public ResponseEntity<Booking> bookFlight(@Valid @RequestBody BookingRequestDTO bookingRequest) {
        Booking savedBooking = bookingService.bookFlight(bookingRequest);
        return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
    }

    // US010: View Booking Details
    @GetMapping
    public ResponseEntity<List<Booking>> viewAllBookings(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    // US011: Cancel Booking / Partial Cancellation
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody PartialCancelRequestDTO cancelRequest,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "role", required = false) String roleParam) {
        
        String role = roleHeader != null ? roleHeader : roleParam;
        Booking updatedBooking = bookingService.cancelPartialOrFull(bookingId, cancelRequest, role);
        return ResponseEntity.ok(updatedBooking);
    }
}
