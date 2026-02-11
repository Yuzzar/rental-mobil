package com.rentcar.backend_rent_car.service;

import com.rentcar.backend_rent_car.dto.UserRequest;
import com.rentcar.backend_rent_car.dto.UserResponse;
import com.rentcar.backend_rent_car.entity.User;
import com.rentcar.backend_rent_car.enums.UserRole;
import com.rentcar.backend_rent_car.exception.BadRequestException;
import com.rentcar.backend_rent_car.exception.ResourceNotFoundException;
import com.rentcar.backend_rent_car.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Service - CRUD operations untuk data user (admin only).
 * Juga digunakan untuk mendapatkan profile user sendiri.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan dengan ID: " + id));
        return mapToResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan: " + username));
        return mapToResponse(user);
    }

    /**
     * Buat user baru (admin only).
     * Admin bisa membuat user dengan role ADMIN atau USER.
     */
    public UserResponse createUser(UserRequest request, String adminUsername) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username sudah digunakan");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email sudah digunakan");
        }

        UserRole role = UserRole.USER;
        if (request.getRole() != null) {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(
                        request.getPassword() != null ? request.getPassword() : "defaultPass123"))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(role)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        activityLogService.log(
                adminUsername, "ADMIN",
                "CREATE_USER", "User", savedUser.getId(),
                "User baru dibuat oleh admin: " + savedUser.getUsername() + " (role: " + savedUser.getRole() + ")");

        return mapToResponse(savedUser);
    }

    /**
     * Update user (admin only).
     */
    public UserResponse updateUser(Long id, UserRequest request, String adminUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan dengan ID: " + id));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        User updatedUser = userRepository.save(user);

        activityLogService.log(
                adminUsername, "ADMIN",
                "UPDATE_USER", "User", updatedUser.getId(),
                "User diupdate: " + updatedUser.getUsername());

        return mapToResponse(updatedUser);
    }

    /**
     * Hapus user (admin only). Soft delete - set active = false.
     */
    public void deleteUser(Long id, String adminUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan dengan ID: " + id));

        user.setActive(false);
        userRepository.save(user);

        activityLogService.log(
                adminUsername, "ADMIN",
                "DELETE_USER", "User", user.getId(),
                "User dihapus (deactivated): " + user.getUsername());
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
