package com.rentcar.backend_rent_car.controller;

import com.rentcar.backend_rent_car.dto.ApiResponse;
import com.rentcar.backend_rent_car.dto.RentalRequest;
import com.rentcar.backend_rent_car.dto.RentalResponse;
import com.rentcar.backend_rent_car.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Rental Controller
 * - POST /api/rentals → User buat rental baru
 * - GET /api/rentals/my → User lihat riwayat rental
 * - PUT /api/rentals/{id}/cancel → User cancel rental
 * - GET /api/admin/rentals → Admin lihat semua rental
 * - PUT /api/admin/rentals/{id}/approve → Admin approve
 * - PUT /api/admin/rentals/{id}/reject → Admin reject
 * - PUT /api/admin/rentals/{id}/complete → Admin selesaikan rental
 */
@RestController
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // ==================== USER ENDPOINTS ====================

    /**
     * User membuat rental baru.
     * Username diambil dari JWT token (Authentication object).
     */
    @PostMapping("/api/rentals")
    public ResponseEntity<ApiResponse<RentalResponse>> createRental(
            @Valid @RequestBody RentalRequest request,
            Authentication authentication) {
        RentalResponse rental = rentalService.createRental(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Rental berhasil dibuat", rental));
    }

    /**
     * User melihat riwayat rental mereka.
     */
    @GetMapping("/api/rentals/my")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getMyRentals(
            Authentication authentication) {
        List<RentalResponse> rentals = rentalService.getUserRentals(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(rentals));
    }

    /**
     * User cancel rental (hanya jika masih PENDING).
     */
    @PutMapping("/api/rentals/{id}/cancel")
    public ResponseEntity<ApiResponse<RentalResponse>> cancelRental(
            @PathVariable Long id,
            Authentication authentication) {
        RentalResponse rental = rentalService.cancelRental(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Rental berhasil dibatalkan", rental));
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/api/admin/rentals")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getAllRentals() {
        List<RentalResponse> rentals = rentalService.getAllRentals();
        return ResponseEntity.ok(ApiResponse.success(rentals));
    }

    @PutMapping("/api/admin/rentals/{id}/approve")
    public ResponseEntity<ApiResponse<RentalResponse>> approveRental(
            @PathVariable Long id,
            Authentication authentication) {
        RentalResponse rental = rentalService.approveRental(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Rental berhasil di-approve", rental));
    }

    @PutMapping("/api/admin/rentals/{id}/reject")
    public ResponseEntity<ApiResponse<RentalResponse>> rejectRental(
            @PathVariable Long id,
            Authentication authentication) {
        RentalResponse rental = rentalService.rejectRental(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Rental berhasil di-reject", rental));
    }

    @PutMapping("/api/admin/rentals/{id}/complete")
    public ResponseEntity<ApiResponse<RentalResponse>> completeRental(
            @PathVariable Long id,
            Authentication authentication) {
        RentalResponse rental = rentalService.completeRental(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Rental berhasil diselesaikan", rental));
    }
}
