package com.ams.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "passengers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long passengerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore
    private Booking booking;

    @NotBlank(message = "Passenger Name is required")
    @Column(nullable = false, length = 60)
    private String name;

    @NotNull(message = "Passenger Age is required")
    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 120, message = "Age cannot exceed 120")
    @Column(nullable = false)
    private Integer age;

    @NotBlank(message = "Passenger Gender is required")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    @Column(nullable = false, length = 10)
    private String gender;

    @Column(length = 100)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(length = 20)
    private String seatNumber;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "Booked"; // "Booked" or "Cancelled"
}
