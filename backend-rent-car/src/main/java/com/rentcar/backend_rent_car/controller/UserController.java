package com.rentcar.backend_rent_car.controller;

import com.rentcar.backend_rent_car.dto.ApiResponse;
import com.rentcar.backend_rent_car.dto.UserRequest;
import com.rentcar.backend_rent_car.dto.UserResponse;
import com.rentcar.backend_rent_car.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Controller
 * - /api/admin/users/** → Admin CRUD user
 * - /api/users/profile → User lihat profil sendiri
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/api/admin/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/api/admin/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/api/admin/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {
        UserResponse user = userService.createUser(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("User berhasil dibuat", user));
    }

    @PutMapping("/api/admin/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request,
            Authentication authentication) {
        UserResponse user = userService.updateUser(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("User berhasil diupdate", user));
    }

    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {
        userService.deleteUser(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("User berhasil dihapus", null));
    }

    // ==================== USER ENDPOINTS ====================

    @GetMapping("/api/users/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        UserResponse user = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
