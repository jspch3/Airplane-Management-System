package com.ams.service;

import com.ams.domain.*;
import com.ams.dto.BookingRequestDTO;
import com.ams.dto.PassengerDTO;
import com.ams.dto.PartialCancelRequestDTO;
import com.ams.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final CarrierRepository carrierRepository;

    @Transactional
    public Booking bookFlight(BookingRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new RuntimeException("Flight not found with ID: " + request.getFlightId()));

        Carrier carrier = carrierRepository.findById(flight.getCarrierId())
                .orElseThrow(() -> new RuntimeException("Carrier not found with ID: " + flight.getCarrierId()));

        int requestedSeats = request.getNoOfSeats();
        String seatCategory = request.getSeatCategory().toUpperCase();

        // 1. Verify seat availability per category
        if ("ECONOMY".equals(seatCategory)) {
            int available = flight.getSeatCapacityEconomyClass() - flight.getBookedSeatsEconomyClass();
            if (requestedSeats > available) {
                throw new RuntimeException("Only " + available + " Economy seats remaining for Flight #" + flight.getFlightId());
            }
            flight.setBookedSeatsEconomyClass(flight.getBookedSeatsEconomyClass() + requestedSeats);
        } else if ("BUSINESS".equals(seatCategory)) {
            int available = flight.getSeatCapacityBusinessClass() - flight.getBookedSeatsBusinessClass();
            if (requestedSeats > available) {
                throw new RuntimeException("Only " + available + " Business seats remaining for Flight #" + flight.getFlightId());
            }
            flight.setBookedSeatsBusinessClass(flight.getBookedSeatsBusinessClass() + requestedSeats);
        } else if ("EXECUTIVE".equals(seatCategory)) {
            int available = flight.getSeatCapacityExecutiveClass() - flight.getBookedSeatsExecutiveClass();
            if (requestedSeats > available) {
                throw new RuntimeException("Only " + available + " Executive seats remaining for Flight #" + flight.getFlightId());
            }
            flight.setBookedSeatsExecutiveClass(flight.getBookedSeatsExecutiveClass() + requestedSeats);
        } else {
            throw new RuntimeException("Invalid seat category: " + seatCategory);
        }

        flightRepository.save(flight);

        // 2. Select base fare per seat based on selected seat class
        double baseFarePerSeat = flight.getEconomyClassFare() != null ? flight.getEconomyClassFare() : flight.getAirFare();
        if ("BUSINESS".equals(seatCategory)) {
            baseFarePerSeat = flight.getBusinessClassFare() != null ? flight.getBusinessClassFare() : (flight.getAirFare() * 1.8);
        } else if ("EXECUTIVE".equals(seatCategory)) {
            baseFarePerSeat = flight.getExecutiveClassFare() != null ? flight.getExecutiveClassFare() : (flight.getAirFare() * 2.8);
        }

        double grossAmount = baseFarePerSeat * requestedSeats;

        // 3. Discount Engine Calculation (Rupees ₹)
        long daysAhead = ChronoUnit.DAYS.between(LocalDate.now(), request.getDateOfTravel());
        double advanceDiscountPct = 0.0;
        if (daysAhead >= 90) advanceDiscountPct = carrier.getDiscount90DaysAdvanceBooking();
        else if (daysAhead >= 60) advanceDiscountPct = carrier.getDiscount60DaysAdvanceBooking();
        else if (daysAhead >= 30) advanceDiscountPct = carrier.getDiscount30DaysAdvanceBooking();

        double advanceDiscountAmount = (grossAmount * advanceDiscountPct) / 100.0;

        // Tier discount
        double tierDiscountPct = 0.0;
        String userCategory = user.getCustomerCategory() != null ? user.getCustomerCategory().toUpperCase() : "REGULAR";
        if ("SILVER".equals(userCategory)) tierDiscountPct = carrier.getSilverUserDiscount();
        else if ("GOLD".equals(userCategory)) tierDiscountPct = carrier.getGoldUserDiscount();
        else if ("PLATINUM".equals(userCategory)) tierDiscountPct = carrier.getPlatinumUserDiscount();

        double tierDiscountAmount = (grossAmount * tierDiscountPct) / 100.0;

        // Bulk discount
        double bulkDiscountPct = 0.0;
        if (requestedSeats >= 10) bulkDiscountPct = carrier.getBulkBookingDiscount();
        double bulkDiscountAmount = (grossAmount * bulkDiscountPct) / 100.0;

        double totalDiscount = advanceDiscountAmount + tierDiscountAmount + bulkDiscountAmount;
        double netPayableAmount = Math.max(0, grossAmount - totalDiscount);

        String txnId = "TXN-AMS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String depTime = flight.getDepartureTime() != null ? flight.getDepartureTime() : "10:30 AM";
        String arrTime = flight.getArrivalTime() != null ? flight.getArrivalTime() : "01:45 PM";

        // 4. Construct Booking entity
        Booking booking = Booking.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .flightId(flight.getFlightId())
                .flightName(flight.getCarrierName() + " (" + flight.getOrigin() + " to " + flight.getDestination() + ")")
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .departureTime(depTime)
                .arrivalTime(arrTime)
                .noOfSeats(requestedSeats)
                .seatCategory(seatCategory)
                .dateOfTravel(request.getDateOfTravel())
                .bookingDate(LocalDate.now())
                .grossAmount(grossAmount)
                .advanceDiscountAmount(advanceDiscountAmount)
                .tierDiscountAmount(tierDiscountAmount)
                .bulkDiscountAmount(bulkDiscountAmount)
                .totalDiscountAmount(totalDiscount)
                .bookingAmount(netPayableAmount)
                .bookingStatus("BOOKED")
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CARD")
                .transactionId(txnId)
                .refundAmount(0.0)
                .build();

        List<Passenger> passengers = new ArrayList<>();
        String[] seatCols = {"A", "B", "C", "D", "E", "F"};
        int rowOffset = "BUSINESS".equals(seatCategory) ? 3 : ("EXECUTIVE".equals(seatCategory) ? 1 : 12);

        if (request.getPassengers() != null) {
            for (int i = 0; i < request.getPassengers().size(); i++) {
                PassengerDTO pDto = request.getPassengers().get(i);
                String seatNum = pDto.getSeatNumber();
                if (seatNum == null || seatNum.isBlank()) {
                    seatNum = (rowOffset + (i / 6)) + seatCols[i % 6];
                }

                Passenger p = Passenger.builder()
                        .name(pDto.getName())
                        .age(pDto.getAge())
                        .gender(pDto.getGender())
                        .email(pDto.getEmail())
                        .phone(pDto.getPhone())
                        .seatNumber(seatNum)
                        .status("BOOKED")
                        .booking(booking)
                        .build();
                passengers.add(p);
            }
        }
        booking.setPassengers(passengers);

        return bookingRepository.save(booking);
    }

    private Booking populateFlightDetails(Booking b) {
        if (b.getDepartureTime() == null || b.getDepartureTime().isBlank() || b.getArrivalTime() == null || b.getArrivalTime().isBlank()) {
            Flight f = flightRepository.findById(b.getFlightId()).orElse(null);
            if (f != null) {
                if (b.getOrigin() == null || b.getOrigin().isBlank()) b.setOrigin(f.getOrigin());
                if (b.getDestination() == null || b.getDestination().isBlank()) b.setDestination(f.getDestination());
                b.setDepartureTime(f.getDepartureTime() != null ? f.getDepartureTime() : "10:30 AM");
                b.setArrivalTime(f.getArrivalTime() != null ? f.getArrivalTime() : "01:45 PM");
            } else {
                b.setDepartureTime("10:30 AM");
                b.setArrivalTime("01:45 PM");
            }
        }

        if (b.getPassengers() != null) {
            String[] seatCols = {"A", "B", "C", "D", "E", "F"};
            int rowOffset = "BUSINESS".equalsIgnoreCase(b.getSeatCategory()) ? 3 : ("EXECUTIVE".equalsIgnoreCase(b.getSeatCategory()) ? 1 : 12);
            for (int i = 0; i < b.getPassengers().size(); i++) {
                Passenger p = b.getPassengers().get(i);
                if (p.getSeatNumber() == null || p.getSeatNumber().isBlank()) {
                    p.setSeatNumber((rowOffset + (i / 6)) + seatCols[i % 6]);
                }
            }
        }

        return b;
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        List<Booking> list = bookingRepository.findByUserIdOrderByBookingIdDesc(userId);
        list.forEach(this::populateFlightDetails);
        return list;
    }

    public Booking getBookingById(Long bookingId) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));
        return populateFlightDetails(b);
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = bookingRepository.findAllByOrderByBookingIdDesc();
        list.forEach(this::populateFlightDetails);
        return list;
    }

    @Transactional
    public Booking cancelPartialOrFull(Long bookingId, PartialCancelRequestDTO request) {
        return cancelPartialBooking(bookingId, request.getPassengerIds());
    }

    @Transactional
    public Booking cancelPartialBooking(Long bookingId, List<Long> passengerIdsToCancel) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        if ("CANCELLED".equals(booking.getBookingStatus())) {
            throw new RuntimeException("Booking is already fully cancelled.");
        }

        Flight flight = flightRepository.findById(booking.getFlightId()).orElse(null);
        Carrier carrier = flight != null ? carrierRepository.findById(flight.getCarrierId()).orElse(null) : null;

        int cancelledCount = 0;
        for (Passenger p : booking.getPassengers()) {
            if (passengerIdsToCancel.contains(p.getPassengerId()) && !"CANCELLED".equals(p.getStatus())) {
                p.setStatus("CANCELLED");
                cancelledCount++;
            }
        }

        if (cancelledCount == 0) {
            throw new RuntimeException("No valid active passengers selected for cancellation.");
        }

        // Restore seat capacity to Flight schedule
        if (flight != null) {
            String category = booking.getSeatCategory().toUpperCase();
            if ("ECONOMY".equals(category)) {
                flight.setBookedSeatsEconomyClass(Math.max(0, flight.getBookedSeatsEconomyClass() - cancelledCount));
            } else if ("BUSINESS".equals(category)) {
                flight.setBookedSeatsBusinessClass(Math.max(0, flight.getBookedSeatsBusinessClass() - cancelledCount));
            } else if ("EXECUTIVE".equals(category)) {
                flight.setBookedSeatsExecutiveClass(Math.max(0, flight.getBookedSeatsExecutiveClass() - cancelledCount));
            }
            flightRepository.save(flight);
        }

        // Calculate Proportional Refund based on days prior to travel
        long daysPrior = ChronoUnit.DAYS.between(LocalDate.now(), booking.getDateOfTravel());
        double refundPct = 0.0;
        if (carrier != null) {
            if (daysPrior >= 20) refundPct = carrier.getRefund20DaysOrMoreBeforeTravelDate();
            else if (daysPrior >= 10) refundPct = carrier.getRefund10DaysBeforeTravelDate();
            else refundPct = carrier.getRefund2DaysBeforeTravelDate();
        } else {
            refundPct = 50.0;
        }

        double singleSeatNetFare = booking.getNetPayableAmount() / booking.getNoOfSeats();
        double additionalRefund = (singleSeatNetFare * cancelledCount * refundPct) / 100.0;

        booking.setRefundAmount(booking.getRefundAmount() + additionalRefund);

        long activeCount = booking.getPassengers().stream().filter(p -> !"CANCELLED".equals(p.getStatus())).count();
        if (activeCount == 0) {
            booking.setBookingStatus("CANCELLED");
        } else {
            booking.setBookingStatus("PARTIALLY_CANCELLED");
        }

        Booking saved = bookingRepository.save(booking);
        return populateFlightDetails(saved);
    }
}
