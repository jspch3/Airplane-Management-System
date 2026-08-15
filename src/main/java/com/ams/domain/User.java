package com.ams.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 30, message = "Username must be between 4 and 30 characters")
    @Column(nullable = false, unique = true, length = 30)
    private String userName;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(CUSTOMER|ADMIN)$", message = "Role must be CUSTOMER or ADMIN")
    @Column(nullable = false, length = 15)
    private String role;

    @NotBlank(message = "Customer Category is required")
    @Pattern(regexp = "^(SILVER|GOLD|PLATINUM|REGULAR)$", message = "Category must be SILVER, GOLD, PLATINUM, or REGULAR")
    @Column(nullable = false, length = 15)
    private String customerCategory;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone number must be exactly 10 digits and start with 6, 7, 8, or 9")
    @Column(nullable = false, length = 10)
    private String phone;

    @NotBlank(message = "Email ID is required")
    @Email(message = "Email ID must be a valid email format")
    @Column(nullable = false, length = 100)
    private String emailId;

    @NotBlank(message = "Address 1 is required")
    @Column(nullable = false)
    private String address1;

    private String address2;

    @NotBlank(message = "City is required")
    @Column(nullable = false, length = 50)
    private String city;

    @NotBlank(message = "State is required")
    @Column(nullable = false, length = 50)
    private String state;

    @NotBlank(message = "Zip code is required")
    @Pattern(regexp = "^[0-9]{5,6}$", message = "Zip code must be 5 or 6 digits")
    @Column(nullable = false, length = 10)
    private String zipCode;

    @NotNull(message = "Date of Birth is required")
    @Past(message = "Date of birth must be a past date")
    @Column(nullable = false)
    private LocalDate dob;
}
