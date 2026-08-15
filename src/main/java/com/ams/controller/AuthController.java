package com.ams.controller;

import com.ams.dto.LoginRequestDTO;
import com.ams.dto.LoginResponseDTO;
import com.ams.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> validateLogin(@Valid @RequestBody LoginRequestDTO loginDTO) {
        return ResponseEntity.ok(userService.validateLogin(loginDTO));
    }
}
