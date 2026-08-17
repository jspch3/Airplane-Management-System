package com.ams.controller;

import com.ams.dto.ForgotPasswordRequestDTO.*;
import com.ams.dto.LoginRequestDTO;
import com.ams.dto.LoginResponseDTO;
import com.ams.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> validateLogin(@Valid @RequestBody LoginRequestDTO loginDTO) {
        return ResponseEntity.ok(userService.validateLogin(loginDTO));
    }

    @PostMapping("/forgot-password/mask-phone")
    public ResponseEntity<MaskPhoneResponse> getMaskedPhone(@Valid @RequestBody MaskPhoneRequest request) {
        return ResponseEntity.ok(userService.getMaskedPhoneForIdentity(request.getIdentity()));
    }

    @PostMapping("/forgot-password/verify-mobile")
    public ResponseEntity<Map<String, String>> verifyMobile(@Valid @RequestBody VerifyMobileRequest request) {
        userService.verifyMobileNumber(request.getUserName(), request.getMobileNumber());
        return ResponseEntity.ok(Map.of("message", "Mobile number verified successfully"));
    }

    @PostMapping("/forgot-password/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getUserName(), request.getMobileNumber(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
