package com.ams.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

public class ForgotPasswordRequestDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaskPhoneRequest {
        @NotBlank(message = "Username or Email is required")
        private String identity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaskPhoneResponse {
        private String userName;
        private String maskedPhone;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerifyMobileRequest {
        @NotBlank(message = "Username is required")
        private String userName;

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone number must be 10 digits starting 6-9")
        private String mobileNumber;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResetPasswordRequest {
        @NotBlank(message = "Username is required")
        private String userName;

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone number must be 10 digits starting 6-9")
        private String mobileNumber;

        @NotBlank(message = "New Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        private String newPassword;
    }
}
