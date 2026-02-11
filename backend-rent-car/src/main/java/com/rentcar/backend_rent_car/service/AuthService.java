package com.rentcar.backend_rent_car.service;

import com.rentcar.backend_rent_car.dto.LoginRequest;
import com.rentcar.backend_rent_car.dto.LoginResponse;
import com.rentcar.backend_rent_car.dto.RegisterRequest;
import com.rentcar.backend_rent_car.dto.UserResponse;
import com.rentcar.backend_rent_car.entity.User;
import com.rentcar.backend_rent_car.enums.UserRole;
import com.rentcar.backend_rent_car.exception.BadRequestException;
import com.rentcar.backend_rent_car.repository.UserRepository;
import com.rentcar.backend_rent_car.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Auth Service - menangani proses login dan registrasi.
 * 
 * Alur Login:
 * 1. AuthenticationManager memverifikasi username & password
 * 2. Jika valid, JwtTokenProvider generate JWT token
 * 3. Token dikembalikan ke client untuk digunakan di setiap request
 * 
 * Alur Register:
 * 1. Validasi username dan email belum terpakai
 * 2. Hash password menggunakan BCrypt
 * 3. Simpan user baru dengan role USER
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public LoginResponse login(LoginRequest request) {
        // AuthenticationManager akan memanggil CustomUserDetailsService
        // dan memverifikasi password secara otomatis
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        // Log aktivitas login
        activityLogService.log(
                user.getUsername(), user.getRole().name(),
                "LOGIN", "User", user.getId(),
                "User " + user.getUsername() + " berhasil login");

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    public UserResponse register(RegisterRequest request) {
        // Validasi: cek apakah username sudah ada
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username sudah digunakan");
        }

        // Validasi: cek apakah email sudah ada
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email sudah digunakan");
        }

        // Buat user baru dengan password yang di-hash
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(UserRole.USER) // Default role: USER
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        // Log aktivitas registrasi
        activityLogService.log(
                savedUser.getUsername(), savedUser.getRole().name(),
                "REGISTER", "User", savedUser.getId(),
                "User baru terdaftar: " + savedUser.getUsername());

        return mapToUserResponse(savedUser);
    }

    private UserResponse mapToUserResponse(User user) {
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
