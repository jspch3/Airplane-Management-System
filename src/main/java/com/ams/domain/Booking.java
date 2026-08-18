package com.ams.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(length = 20, unique = true)
    private String pnr;

    @NotNull(message = "User ID is required")
    @Column(nullable = false)
    private Long userId;

    @NotBlank(message = "User Name is required")
    @Column(nullable = false, length = 50)
    private String userName;

    private String userEmail;

    private String userPhone;

    private String customerCategory;

    @NotNull(message = "Flight ID is required")
    @Column(nullable = false)
    private Long flightId;

    @NotBlank(message = "Flight Name / Carrier is required")
    @Column(nullable = false, length = 100)
    private String flightName;

    private String origin;

    private String destination;

    private String departureTime;

    private String arrivalTime;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "At least 1 seat is required")
    @Column(nullable = false)
    private Integer noOfSeats;

    @NotBlank(message = "Seat Category is required")
    @Pattern(regexp = "^(BUSINESS|ECONOMY|EXECUTIVE)$", message = "Seat Category must be BUSINESS, ECONOMY, or EXECUTIVE")
    @Column(nullable = false, length = 15)
    private String seatCategory;

    @NotNull(message = "Date of travel is required")
    @FutureOrPresent(message = "Date of travel must be today or a future date")
    @Column(nullable = false)
    private LocalDate dateOfTravel;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Builder.Default
    @Column(nullable = false, length = 25)
    private String bookingStatus = "BOOKED"; // "BOOKED", "PARTIALLY_CANCELLED", "CANCELLED"

    @Column(nullable = false)
    private Double grossAmount;

    @Builder.Default
    private Double advanceDiscountAmount = 0.0;

    @Builder.Default
    private Double tierDiscountAmount = 0.0;

    @Builder.Default
    private Double bulkDiscountAmount = 0.0;

    @Builder.Default
    private Double totalDiscountAmount = 0.0;

    @Builder.Default
    private Double gstAmount = 0.0;

    @Column(nullable = false)
    private Double bookingAmount; // Net payable booking amount

    @Builder.Default
    private Double refundAmount = 0.0;

    private LocalDate cancellationDate;

    private String paymentMethod;

    private String transactionId;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Passenger> passengers = new ArrayList<>();

    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
        passenger.setBooking(this);
    }

    public Double getNetPayableAmount() {
        return bookingAmount != null ? bookingAmount : grossAmount;
    }

    public void setNetPayableAmount(Double netPayableAmount) {
        this.bookingAmount = netPayableAmount;
    }
}
