package com.rentcar.backend_rent_car.config;

import com.rentcar.backend_rent_car.entity.Car;
import com.rentcar.backend_rent_car.entity.User;
import com.rentcar.backend_rent_car.enums.CarStatus;
import com.rentcar.backend_rent_car.enums.UserRole;
import com.rentcar.backend_rent_car.repository.CarRepository;
import com.rentcar.backend_rent_car.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Data Seeder - dijalankan otomatis saat aplikasi start.
 * Membuat data awal:
 * 1. Akun admin default (admin / admin123)
 * 2. Beberapa sample mobil untuk demo
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCars();
    }

    private void seedAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Administrator")
                    .email("admin@rentcar.com")
                    .phone("081234567890")
                    .role(UserRole.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("✅ Admin account created: admin / admin123");
        }
    }

    private void seedCars() {
        if (carRepository.count() == 0) {
            Car[] cars = {
                    Car.builder().brand("Toyota").model("Avanza").year(2023)
                            .licensePlate("B 1234 ABC").color("Putih")
                            .dailyRate(new BigDecimal("350000")).status(CarStatus.AVAILABLE).build(),
                    Car.builder().brand("Honda").model("Brio").year(2023)
                            .licensePlate("B 5678 DEF").color("Merah")
                            .dailyRate(new BigDecimal("300000")).status(CarStatus.AVAILABLE).build(),
                    Car.builder().brand("Toyota").model("Innova").year(2024)
                            .licensePlate("B 9012 GHI").color("Hitam")
                            .dailyRate(new BigDecimal("500000")).status(CarStatus.AVAILABLE).build(),
                    Car.builder().brand("Mitsubishi").model("Xpander").year(2024)
                            .licensePlate("B 3456 JKL").color("Silver")
                            .dailyRate(new BigDecimal("450000")).status(CarStatus.AVAILABLE).build(),
                    Car.builder().brand("Suzuki").model("Ertiga").year(2023)
                            .licensePlate("B 7890 MNO").color("Abu-abu")
                            .dailyRate(new BigDecimal("380000")).status(CarStatus.AVAILABLE).build(),
                    Car.builder().brand("Daihatsu").model("Xenia").year(2022)
                            .licensePlate("B 2345 PQR").color("Biru")
                            .dailyRate(new BigDecimal("320000")).status(CarStatus.AVAILABLE).build(),
                    Car.builder().brand("Toyota").model("Fortuner").year(2024)
                            .licensePlate("B 6789 STU").color("Hitam")
                            .dailyRate(new BigDecimal("800000")).status(CarStatus.AVAILABLE).build(),
                    Car.builder().brand("Honda").model("CR-V").year(2024)
                            .licensePlate("B 1357 VWX").color("Putih")
                            .dailyRate(new BigDecimal("700000")).status(CarStatus.AVAILABLE).build(),
            };

            for (Car car : cars) {
                carRepository.save(car);
            }
            log.info("✅ {} sample cars created", cars.length);
        }
    }
}
