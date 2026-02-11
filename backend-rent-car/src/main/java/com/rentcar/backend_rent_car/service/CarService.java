package com.rentcar.backend_rent_car.service;

import com.rentcar.backend_rent_car.dto.CarRequest;
import com.rentcar.backend_rent_car.dto.CarResponse;
import com.rentcar.backend_rent_car.entity.Car;
import com.rentcar.backend_rent_car.enums.CarStatus;
import com.rentcar.backend_rent_car.exception.ResourceNotFoundException;
import com.rentcar.backend_rent_car.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Car Service - CRUD operations untuk data mobil.
 * 
 * Digunakan oleh:
 * - Admin: create, update, delete mobil
 * - User: melihat daftar mobil yang available, filter berdasarkan brand
 */
@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final ActivityLogService activityLogService;

    /**
     * Ambil semua mobil dengan optional filter.
     */
    public List<CarResponse> getAllCars(String brand, String status) {
        List<Car> cars;

        if (brand != null && !brand.isEmpty()) {
            cars = carRepository.findByBrandContainingIgnoreCase(brand);
        } else if (status != null && !status.isEmpty()) {
            cars = carRepository.findByStatus(CarStatus.valueOf(status.toUpperCase()));
        } else {
            cars = carRepository.findAll();
        }

        return cars.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Ambil mobil yang available saja (untuk modul user).
     */
    public List<CarResponse> getAvailableCars(String brand) {
        List<Car> cars;
        if (brand != null && !brand.isEmpty()) {
            cars = carRepository.findByBrandContainingIgnoreCase(brand).stream()
                    .filter(c -> c.getStatus() == CarStatus.AVAILABLE)
                    .collect(Collectors.toList());
        } else {
            cars = carRepository.findByStatus(CarStatus.AVAILABLE);
        }
        return cars.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Ambil detail mobil berdasarkan ID.
     */
    public CarResponse getCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mobil tidak ditemukan dengan ID: " + id));
        return mapToResponse(car);
    }

    /**
     * Buat mobil baru (admin only).
     */
    public CarResponse createCar(CarRequest request, String adminUsername) {
        Car car = Car.builder()
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .licensePlate(request.getLicensePlate())
                .color(request.getColor())
                .dailyRate(request.getDailyRate())
                .status(CarStatus.AVAILABLE) // Mobil baru selalu AVAILABLE
                .imageUrl(request.getImageUrl())
                .build();

        Car savedCar = carRepository.save(car);

        // Log aktivitas
        activityLogService.log(
                adminUsername, "ADMIN",
                "CREATE_CAR", "Car", savedCar.getId(),
                "Mobil baru ditambahkan: " + savedCar.getBrand() + " " + savedCar.getModel()
                        + " (" + savedCar.getLicensePlate() + ")");

        return mapToResponse(savedCar);
    }

    /**
     * Update data mobil (admin only).
     */
    public CarResponse updateCar(Long id, CarRequest request, String adminUsername) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mobil tidak ditemukan dengan ID: " + id));

        car.setBrand(request.getBrand());
        car.setModel(request.getModel());
        car.setYear(request.getYear());
        car.setLicensePlate(request.getLicensePlate());
        car.setColor(request.getColor());
        car.setDailyRate(request.getDailyRate());
        if (request.getStatus() != null) {
            car.setStatus(CarStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getImageUrl() != null) {
            car.setImageUrl(request.getImageUrl());
        }

        Car updatedCar = carRepository.save(car);

        activityLogService.log(
                adminUsername, "ADMIN",
                "UPDATE_CAR", "Car", updatedCar.getId(),
                "Mobil diupdate: " + updatedCar.getBrand() + " " + updatedCar.getModel());

        return mapToResponse(updatedCar);
    }

    /**
     * Hapus mobil (admin only).
     */
    public void deleteCar(Long id, String adminUsername) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mobil tidak ditemukan dengan ID: " + id));

        activityLogService.log(
                adminUsername, "ADMIN",
                "DELETE_CAR", "Car", car.getId(),
                "Mobil dihapus: " + car.getBrand() + " " + car.getModel()
                        + " (" + car.getLicensePlate() + ")");

        carRepository.delete(car);
    }

    /**
     * Mapping dari entity Car ke DTO CarResponse.
     */
    private CarResponse mapToResponse(Car car) {
        return CarResponse.builder()
                .id(car.getId())
                .brand(car.getBrand())
                .model(car.getModel())
                .year(car.getYear())
                .licensePlate(car.getLicensePlate())
                .color(car.getColor())
                .dailyRate(car.getDailyRate())
                .status(car.getStatus().name())
                .imageUrl(car.getImageUrl())
                .createdAt(car.getCreatedAt())
                .updatedAt(car.getUpdatedAt())
                .build();
    }
}
