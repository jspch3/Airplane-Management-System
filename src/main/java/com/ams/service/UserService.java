package com.ams.service;

import com.ams.domain.User;
import com.ams.dto.LoginRequestDTO;
import com.ams.dto.LoginResponseDTO;
import com.ams.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User registerUser(User user) {
        if (userRepository.existsByUserName(user.getUserName())) {
            throw new IllegalArgumentException("Username '" + user.getUserName() + "' is already taken");
        }
        if (userRepository.existsByEmailId(user.getEmailId())) {
            throw new IllegalArgumentException("Email ID '" + user.getEmailId() + "' is already registered");
        }

        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("CUSTOMER");
        }
        if (user.getCustomerCategory() == null || user.getCustomerCategory().isBlank()) {
            user.setCustomerCategory("REGULAR");
        }

        return userRepository.save(user);
    }

    public User registerAdmin(User user) {
        user.setRole("ADMIN");
        user.setCustomerCategory("REGULAR");
        if (userRepository.existsByUserName(user.getUserName())) {
            throw new IllegalArgumentException("Admin username '" + user.getUserName() + "' is already taken");
        }
        return userRepository.save(user);
    }

    public LoginResponseDTO validateLogin(LoginRequestDTO loginDTO) {
        User user = userRepository.findByUserName(loginDTO.getUserName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return LoginResponseDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .role(user.getRole())
                .customerCategory(user.getCustomerCategory())
                .emailId(user.getEmailId())
                .message("Login successful")
                .build();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));
    }

    public User updateUserProfile(Long userId, User updatedUser) {
        User existing = getUserById(userId);
        if (updatedUser.getPhone() != null && !updatedUser.getPhone().isBlank()) {
            existing.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getEmailId() != null && !updatedUser.getEmailId().isBlank()) {
            existing.setEmailId(updatedUser.getEmailId());
        }
        if (updatedUser.getAddress1() != null && !updatedUser.getAddress1().isBlank()) {
            existing.setAddress1(updatedUser.getAddress1());
        }
        if (updatedUser.getAddress2() != null) {
            existing.setAddress2(updatedUser.getAddress2());
        }
        if (updatedUser.getCity() != null && !updatedUser.getCity().isBlank()) {
            existing.setCity(updatedUser.getCity());
        }
        if (updatedUser.getState() != null && !updatedUser.getState().isBlank()) {
            existing.setState(updatedUser.getState());
        }
        if (updatedUser.getZipCode() != null && !updatedUser.getZipCode().isBlank()) {
            existing.setZipCode(updatedUser.getZipCode());
        }
        if (updatedUser.getDob() != null) {
            existing.setDob(updatedUser.getDob());
        }
        if (updatedUser.getCustomerCategory() != null && !updatedUser.getCustomerCategory().isBlank()) {
            existing.setCustomerCategory(updatedUser.getCustomerCategory());
        }
        return userRepository.save(existing);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public long getUserCount() {
        return userRepository.count();
    }
}
