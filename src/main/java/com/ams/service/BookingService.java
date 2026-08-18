package com.ams.service;

import com.ams.domain.*;
import com.ams.dto.BookingRequestDTO;
import com.ams.dto.PassengerDTO;
import com.ams.dto.PartialCancelRequestDTO;
import com.ams.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    private static final String PNR_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateUniquePNR() {
        StringBuilder pnr = new StringBuilder("PNR-");
        for (int i = 0; i < 6; i++) {
            pnr.append(PNR_CHARACTERS.charAt(RANDOM.nextInt(PNR_CHARACTERS.length())));
        }
        return pnr.toString();
    }

    private boolean isPastFlight(Booking booking) {
        if (booking.getDateOfTravel() == null) return false;
        if (booking.getDateOfTravel().isBefore(LocalDate.now())) return true;
        if (booking.getDateOfTravel().isEqual(LocalDate.now())) {
            if (booking.getDepartureTime() != null && !booking.getDepartureTime().isBlank()) {
                try {
                    String timeStr = booking.getDepartureTime().trim();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                    LocalTime depTime = LocalTime.parse(timeStr, formatter);
                    return LocalTime.now().isAfter(depTime);
                } catch (Exception e) {
                    // Fallback comparison
                }
            }
        }
        return false;
    }

    @Transactional
    public Booking bookFlight(BookingRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUserId()));

        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new IllegalArgumentException("Flight not found with ID: " + request.getFlightId()));

        Carrier carrier = carrierRepository.findById(flight.getCarrierId())
                .orElseThrow(() -> new IllegalArgumentException("Carrier not found with ID: " + flight.getCarrierId()));

        if (flight.getOrigin() != null && flight.getOrigin().equalsIgnoreCase(flight.getDestination())) {
            throw new IllegalArgumentException("Origin and Destination cities must be different.");
        }

        int requestedSeats = request.getNoOfSeats();
        if (requestedSeats < 1 || requestedSeats > 6) {
            throw new IllegalArgumentException("A customer can book a maximum of 6 tickets only per booking transaction.");
        }

        String seatCategory = request.getSeatCategory().toUpperCase();

        // 1. Verify seat availability per category
        if ("ECONOMY".equals(seatCategory)) {
            int available = flight.getSeatCapacityEconomyClass() - flight.getBookedSeatsEconomyClass();
            if (requestedSeats > available) {
                throw new IllegalArgumentException("Only " + available + " Economy seats remaining for Flight #" + flight.getFlightId());
            }
            flight.setBookedSeatsEconomyClass(flight.getBookedSeatsEconomyClass() + requestedSeats);
        } else if ("BUSINESS".equals(seatCategory)) {
            int available = flight.getSeatCapacityBusinessClass() - flight.getBookedSeatsBusinessClass();
            if (requestedSeats > available) {
                throw new IllegalArgumentException("Only " + available + " Business seats remaining for Flight #" + flight.getFlightId());
            }
            flight.setBookedSeatsBusinessClass(flight.getBookedSeatsBusinessClass() + requestedSeats);
        } else if ("EXECUTIVE".equals(seatCategory)) {
            int available = flight.getSeatCapacityExecutiveClass() - flight.getBookedSeatsExecutiveClass();
            if (requestedSeats > available) {
                throw new IllegalArgumentException("Only " + available + " Executive seats remaining for Flight #" + flight.getFlightId());
            }
            flight.setBookedSeatsExecutiveClass(flight.getBookedSeatsExecutiveClass() + requestedSeats);
        } else {
            throw new IllegalArgumentException("Invalid seat category: " + seatCategory);
        }

        flightRepository.save(flight);

        // 2. Select base fare per seat based on selected seat class (Tamper-proof calculation on server)
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

        // Bulk discount (Applicable for more than 4 seats, i.e. 5 or 6 seats)
        double bulkDiscountPct = 0.0;
        if (requestedSeats > 4) bulkDiscountPct = carrier.getBulkBookingDiscount();
        double bulkDiscountAmount = (grossAmount * bulkDiscountPct) / 100.0;

        double totalDiscount = advanceDiscountAmount + tierDiscountAmount + bulkDiscountAmount;
        double netPayableAmount = Math.max(1.0, grossAmount - totalDiscount);

        String txnId = "TXN-AMS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String pnrCode = generateUniquePNR();

        String depTime = flight.getDepartureTime() != null ? flight.getDepartureTime() : "10:30 AM";
        String arrTime = flight.getArrivalTime() != null ? flight.getArrivalTime() : "01:45 PM";

        // 4. Construct Booking entity
        Booking booking = Booking.builder()
                .pnr(pnrCode)
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getEmailId())
                .userPhone(user.getPhone())
                .customerCategory(user.getCustomerCategory() != null ? user.getCustomerCategory() : "REGULAR")
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
        if (b.getPnr() == null || b.getPnr().isBlank()) {
            b.setPnr("PNR-" + String.format("%06d", b.getBookingId()));
        }

        if (b.getUserEmail() == null || b.getUserPhone() == null) {
            userRepository.findById(b.getUserId()).ifPresent(u -> {
                if (b.getUserEmail() == null) b.setUserEmail(u.getEmailId());
                if (b.getUserPhone() == null) b.setUserPhone(u.getPhone());
                if (b.getCustomerCategory() == null) b.setCustomerCategory(u.getCustomerCategory());
            });
        }

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
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));
        return populateFlightDetails(b);
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = bookingRepository.findAllByOrderByBookingIdDesc();
        list.forEach(this::populateFlightDetails);
        return list;
    }

    @Transactional
    public Booking cancelPartialOrFull(Long bookingId, PartialCancelRequestDTO request, String requestingRole) {
        if ("ADMIN".equalsIgnoreCase(requestingRole)) {
            throw new IllegalArgumentException("Administrators are not permitted to cancel customer flight bookings.");
        }
        return cancelPartialBooking(bookingId, request.getPassengerIds());
    }

    @Transactional
    public Booking cancelPartialBooking(Long bookingId, List<Long> passengerIdsToCancel) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        if ("CANCELLED".equals(booking.getBookingStatus())) {
            throw new IllegalArgumentException("Booking is already fully cancelled.");
        }

        if (isPastFlight(booking)) {
            throw new IllegalArgumentException("Cannot cancel booking for a flight journey that has already passed/departed.");
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
            throw new IllegalArgumentException("No valid active passengers selected for cancellation.");
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

        // Calculate Proportional Refund based on days prior to travel: <2 days (max 20%), 2-19 days (max 40%), >=20 days (max 75%)
        long daysPrior = ChronoUnit.DAYS.between(LocalDate.now(), booking.getDateOfTravel());
        double refundPct = 20.0;
        if (carrier != null) {
            if (daysPrior >= 20) {
                refundPct = Math.min(75.0, carrier.getRefund20DaysOrMoreBeforeTravelDate() != null ? carrier.getRefund20DaysOrMoreBeforeTravelDate() : 75.0);
            } else if (daysPrior >= 2) {
                refundPct = Math.min(40.0, carrier.getRefund10DaysBeforeTravelDate() != null ? carrier.getRefund10DaysBeforeTravelDate() : 40.0);
            } else {
                refundPct = Math.min(20.0, carrier.getRefund2DaysBeforeTravelDate() != null ? carrier.getRefund2DaysBeforeTravelDate() : 20.0);
            }
        } else {
            if (daysPrior >= 20) refundPct = 75.0;
            else if (daysPrior >= 2) refundPct = 40.0;
            else refundPct = 20.0;
        }

        double singleSeatNetFare = booking.getNetPayableAmount() / booking.getNoOfSeats();
        double additionalRefund = (singleSeatNetFare * cancelledCount * refundPct) / 100.0;

        booking.setRefundAmount(booking.getRefundAmount() + additionalRefund);

        long activeCount = booking.getPassengers().stream().filter(p -> !"CANCELLED".equals(p.getStatus())).count();
        if (activeCount == 0) {
            booking.setBookingStatus("CANCELLED");
            booking.setCancellationDate(LocalDate.now());
        } else {
            booking.setBookingStatus("PARTIALLY_CANCELLED");
            booking.setCancellationDate(LocalDate.now());
        }

        Booking saved = bookingRepository.save(booking);
        return populateFlightDetails(saved);
    }
}
