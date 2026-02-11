package com.rentcar.backend_rent_car.controller;

import com.rentcar.backend_rent_car.dto.ApiResponse;
import com.rentcar.backend_rent_car.dto.CarRequest;
import com.rentcar.backend_rent_car.dto.CarResponse;
import com.rentcar.backend_rent_car.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Car Controller
 * - GET /api/cars → Public, bisa diakses tanpa login (lihat mobil available)
 * - POST/PUT/DELETE → Admin only
 */
@RestController
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    // ==================== PUBLIC ENDPOINTS ====================

    /**
     * List semua mobil dengan filter optional.
     * Bisa diakses tanpa login untuk user yang browsing.
     */
    @GetMapping("/api/cars")
    public ResponseEntity<ApiResponse<List<CarResponse>>> getAllCars(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String status) {
        List<CarResponse> cars = carService.getAllCars(brand, status);
        return ResponseEntity.ok(ApiResponse.success(cars));
    }

    /**
     * Detail mobil by ID.
     */
    @GetMapping("/api/cars/{id}")
    public ResponseEntity<ApiResponse<CarResponse>> getCarById(@PathVariable Long id) {
        CarResponse car = carService.getCarById(id);
        return ResponseEntity.ok(ApiResponse.success(car));
    }

    // ==================== ADMIN ENDPOINTS ====================

    @PostMapping("/api/admin/cars")
    public ResponseEntity<ApiResponse<CarResponse>> createCar(
            @Valid @RequestBody CarRequest request,
            Authentication authentication) {
        CarResponse car = carService.createCar(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Mobil berhasil ditambahkan", car));
    }

    @PutMapping("/api/admin/cars/{id}")
    public ResponseEntity<ApiResponse<CarResponse>> updateCar(
            @PathVariable Long id,
            @Valid @RequestBody CarRequest request,
            Authentication authentication) {
        CarResponse car = carService.updateCar(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Mobil berhasil diupdate", car));
    }

    @DeleteMapping("/api/admin/cars/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCar(
            @PathVariable Long id,
            Authentication authentication) {
        carService.deleteCar(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Mobil berhasil dihapus", null));
    }
}
