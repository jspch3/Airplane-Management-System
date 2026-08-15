package com.ams.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "At least 1 seat is required")
    private Integer noOfSeats;

    @NotBlank(message = "Seat Category is required")
    @Pattern(regexp = "^(BUSINESS|ECONOMY|EXECUTIVE)$", message = "Seat Category must be BUSINESS, ECONOMY, or EXECUTIVE")
    private String seatCategory;

    @NotNull(message = "Date of Travel is required")
    @FutureOrPresent(message = "Date of travel must be today or a future date")
    private LocalDate dateOfTravel;

    @NotEmpty(message = "Passenger details must be provided for all booked seats")
    @Valid
    private List<PassengerDTO> passengers;

    // Payment details
    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // "CARD", "UPI", "NETBANKING"

    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String upiId;
}
