package com.ams.service;

import com.ams.domain.User;
import com.ams.dto.ForgotPasswordRequestDTO.*;
import com.ams.dto.LoginRequestDTO;
import com.ams.dto.LoginResponseDTO;
import com.ams.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private void validateAge(LocalDate dob) {
        if (dob == null) {
            throw new IllegalArgumentException("Date of Birth is required");
        }
        long age = ChronoUnit.YEARS.between(dob, LocalDate.now());
        if (age < 18 || age > 120) {
            throw new IllegalArgumentException("User age must be strictly between 18 and 120 years old");
        }
    }

    public User registerUser(User user) {
        if (userRepository.existsByUserName(user.getUserName())) {
            throw new IllegalArgumentException("Username '" + user.getUserName() + "' is already taken");
        }
        if (userRepository.existsByEmailId(user.getEmailId())) {
            throw new IllegalArgumentException("Email ID '" + user.getEmailId() + "' is already registered");
        }
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new IllegalArgumentException("Mobile number '" + user.getPhone() + "' is already registered");
        }

        validateAge(user.getDob());

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
        if (userRepository.existsByEmailId(user.getEmailId())) {
            throw new IllegalArgumentException("Email ID '" + user.getEmailId() + "' is already registered");
        }
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new IllegalArgumentException("Mobile number '" + user.getPhone() + "' is already registered");
        }
        validateAge(user.getDob());
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

    public MaskPhoneResponse getMaskedPhoneForIdentity(String identity) {
        User user = userRepository.findByUserName(identity)
                .or(() -> userRepository.findByEmailId(identity))
                .orElseThrow(() -> new IllegalArgumentException("No account found with username or email: " + identity));

        String rawPhone = user.getPhone();
        String masked = "*******" + (rawPhone.length() >= 2 ? rawPhone.substring(rawPhone.length() - 2) : "00");

        return MaskPhoneResponse.builder()
                .userName(user.getUserName())
                .maskedPhone(masked)
                .build();
    }

    public boolean verifyMobileNumber(String userName, String mobileNumber) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userName));

        if (!user.getPhone().equals(mobileNumber)) {
            throw new IllegalArgumentException("Mobile number not matched");
        }

        return true;
    }

    public void resetPassword(String userName, String mobileNumber, String newPassword) {
        verifyMobileNumber(userName, mobileNumber);
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userName));

        user.setPassword(newPassword);
        userRepository.save(user);
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
            validateAge(updatedUser.getDob());
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
