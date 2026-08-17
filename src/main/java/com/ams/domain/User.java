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
    @Pattern(regexp = "^[a-zA-Z0-9]{4,30}$", message = "Username must be 4 to 30 alphanumeric characters (symbols and underscore-only strings are rejected)")
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
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._%+-]*@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email ID must start with an alphabetic character and be a valid RFC-compliant email address")
    @Column(nullable = false, length = 100)
    private String emailId;

    @NotBlank(message = "Address 1 is required")
    @Column(nullable = false)
    private String address1;

    private String address2;

    @NotBlank(message = "City is required")
    @Pattern(regexp = "^[A-Za-z\\s]{3,50}$", message = "City must contain at least 3 alphabetic characters")
    @Column(nullable = false, length = 50)
    private String city;

    @NotBlank(message = "State is required")
    @Pattern(regexp = "^[A-Za-z\\s]{3,50}$", message = "State must contain at least 3 alphabetic characters")
    @Column(nullable = false, length = 50)
    private String state;

    @NotBlank(message = "Zip code is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Pincode must be a 6-digit Indian postal code starting 1-9")
    @Column(nullable = false, length = 10)
    private String zipCode;

    @NotNull(message = "Date of Birth is required")
    @Past(message = "Date of birth must be a past date")
    @Column(nullable = false)
    private LocalDate dob;
}
