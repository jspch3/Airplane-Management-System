package com.ams.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "carriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long carrierId;

    @NotBlank(message = "Carrier Name is required")
    @Size(max = 50, message = "Carrier Name cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String carrierName;

    // Discount Percentages
    @NotNull(message = "30 Days Advance Booking Discount is required")
    @Min(0) @Max(100)
    @Column(nullable = false)
    private Double discount30DaysAdvanceBooking;

    @NotNull(message = "60 Days Advance Booking Discount is required")
    @Min(0) @Max(100)
    @Column(nullable = false)
    private Double discount60DaysAdvanceBooking;

    @NotNull(message = "90 Days Advance Booking Discount is required")
    @Min(0) @Max(100)
    @Column(nullable = false)
    private Double discount90DaysAdvanceBooking;

    @NotNull(message = "Bulk Booking Discount is required")
    @Min(0) @Max(100)
    @Column(nullable = false)
    private Double bulkBookingDiscount;

    @NotNull(message = "Silver User Discount is required")
    @Min(0) @Max(100)
    @Column(nullable = false)
    private Double silverUserDiscount;

    @NotNull(message = "Gold User Discount is required")
    @Min(0) @Max(100)
    @Column(nullable = false)
    private Double goldUserDiscount;

    @NotNull(message = "Platinum User Discount is required")
    @Min(0) @Max(100)
    @Column(nullable = false)
    private Double platinumUserDiscount;

    // Refund Percentages
    @NotNull(message = "2 Days Before Refund Percentage is required")
    @Min(0) @Max(100)
    @Column(nullable = false)
    private Double refund2DaysBeforeTravelDate;

    @NotNull(message = "10 Days Before Refund Percentage is required")
    @Min(0) @Max(100)
    @Column(nullable = false)
    private Double refund10DaysBeforeTravelDate;

    @NotNull(message = "20 Days or More Refund Percentage is required")
    @Min(0) @Max(100)
    @Column(nullable = false)
    private Double refund20DaysOrMoreBeforeTravelDate;
}
