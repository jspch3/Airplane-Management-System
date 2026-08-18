package com.ams.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "flights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightId;

    @NotNull(message = "Carrier ID is required")
    private Long carrierId;

    @NotBlank(message = "Carrier Name is required")
    private String carrierName;

    @NotBlank(message = "Origin is required")
    private String origin;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Schedule Date is required")
    private LocalDate scheduleDate;

    @NotBlank(message = "Departure time is required")
    private String departureTime;

    @NotBlank(message = "Arrival time is required")
    private String arrivalTime;

    @NotNull(message = "AirFare is required")
    @Min(value = 1, message = "AirFare must be greater than 0")
    @Builder.Default
    private Double airFare = 5000.0;

    @NotNull(message = "Economy Class Fare is required")
    @Min(value = 1, message = "Economy Class Fare must be greater than 0")
    @Builder.Default
    private Double economyClassFare = 5000.0;

    @NotNull(message = "Business Class Fare is required")
    @Min(value = 1, message = "Business Class Fare must be greater than 0")
    @Builder.Default
    private Double businessClassFare = 10000.0;

    @NotNull(message = "Executive Class Fare is required")
    @Min(value = 1, message = "Executive Class Fare must be greater than 0")
    @Builder.Default
    private Double executiveClassFare = 15000.0;

    @Min(value = 1, message = "Economy capacity must be at least 1")
    @Max(value = 1000, message = "Economy capacity cannot exceed 1000")
    @Builder.Default
    private Integer seatCapacityEconomyClass = 150;

    @Min(value = 1, message = "Business capacity must be at least 1")
    @Max(value = 1000, message = "Business capacity cannot exceed 1000")
    @Builder.Default
    private Integer seatCapacityBusinessClass = 30;

    @Min(value = 1, message = "Executive capacity must be at least 1")
    @Max(value = 1000, message = "Executive capacity cannot exceed 1000")
    @Builder.Default
    private Integer seatCapacityExecutiveClass = 15;

    @Builder.Default
    private Integer bookedSeatsEconomyClass = 0;

    @Builder.Default
    private Integer bookedSeatsExecutiveClass = 0;

    @Builder.Default
    private Integer bookedSeatsBusinessClass = 0;

    @Builder.Default
    private String flightFrequency = "DAILY";
}
