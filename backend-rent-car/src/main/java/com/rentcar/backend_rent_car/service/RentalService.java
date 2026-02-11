package com.rentcar.backend_rent_car.service;

import com.rentcar.backend_rent_car.dto.RentalRequest;
import com.rentcar.backend_rent_car.dto.RentalResponse;
import com.rentcar.backend_rent_car.entity.Car;
import com.rentcar.backend_rent_car.entity.Rental;
import com.rentcar.backend_rent_car.entity.User;
import com.rentcar.backend_rent_car.enums.CarStatus;
import com.rentcar.backend_rent_car.enums.RentalStatus;
import com.rentcar.backend_rent_car.exception.BadRequestException;
import com.rentcar.backend_rent_car.exception.CarNotAvailableException;
import com.rentcar.backend_rent_car.exception.ResourceNotFoundException;
import com.rentcar.backend_rent_car.repository.CarRepository;
import com.rentcar.backend_rent_car.repository.RentalRepository;
import com.rentcar.backend_rent_car.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rental Service - Logika bisnis utama untuk proses rental.
 * 
 * ===== HANDLE CONCURRENT RENTAL (2 USER RENTAL MOBIL YANG SAMA) =====
 * 
 * Problem: User A dan User B secara bersamaan mencoba rental mobil yang sama
 * di tanggal yang sama. Tanpa proteksi, keduanya bisa berhasil → double
 * booking.
 * 
 * Solusi: Pessimistic Locking
 * 1. Ketika user membuat rental, kita acquire PESSIMISTIC_WRITE lock pada row
 * mobil
 * 2. Selama lock aktif, thread/request lain yang mencoba akses row yang sama
 * harus MENUNGGU sampai lock dilepas
 * 3. Setelah lock didapat, kita cek overlap tanggal rental
 * 4. Jika tidak ada overlap → rental berhasil, lock dilepas
 * 5. Jika ada overlap → throw CarNotAvailableException, lock dilepas
 * 6. Request yang menunggu sekarang bisa acquire lock, tapi akan menemukan
 * bahwa sudah ada rental aktif → ditolak
 * 
 * Ini menjamin hanya 1 rental yang berhasil untuk 1 mobil di tanggal yang sama.
 */
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    /**
     * Buat rental baru.
     * 
     * @Transactional diperlukan untuk pessimistic locking.
     */
    @Transactional
    public RentalResponse createRental(String username, RentalRequest request) {
        // 1. Ambil data user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        // 2. Validasi tanggal
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Tanggal mulai tidak boleh di masa lalu");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Tanggal selesai harus setelah tanggal mulai");
        }

        // 3. Acquire PESSIMISTIC LOCK pada mobil
        // Thread lain yang mencoba rental mobil ini akan MENUNGGU di sini
        Car car = carRepository.findByIdWithLock(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Mobil tidak ditemukan"));

        // 4. Cek status mobil
        if (car.getStatus() == CarStatus.MAINTENANCE) {
            throw new CarNotAvailableException("Mobil sedang dalam maintenance");
        }

        // 5. Cek apakah ada rental yang overlap di tanggal tersebut
        // Ini adalah validasi CRITICAL untuk mencegah double booking
        boolean hasOverlap = rentalRepository.existsOverlappingRental(
                car.getId(), request.getStartDate(), request.getEndDate());

        if (hasOverlap) {
            throw new CarNotAvailableException(
                    "Mobil " + car.getBrand() + " " + car.getModel()
                            + " sudah dirental di tanggal tersebut. Silakan pilih tanggal lain.");
        }

        // 6. Kalkulasi total cost
        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        BigDecimal totalCost = car.getDailyRate().multiply(BigDecimal.valueOf(days));

        // 7. Buat rental
        Rental rental = Rental.builder()
                .user(user)
                .car(car)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalCost(totalCost)
                .status(RentalStatus.PENDING)
                .build();

        Rental savedRental = rentalRepository.save(rental);

        // 8. Log aktivitas
        activityLogService.log(
                username, user.getRole().name(),
                "CREATE_RENTAL", "Rental", savedRental.getId(),
                "Rental baru: " + car.getBrand() + " " + car.getModel()
                        + " (" + request.getStartDate() + " s/d " + request.getEndDate() + ")"
                        + " oleh user: " + username);

        return mapToResponse(savedRental);
    }

    /**
     * Admin approve rental → status jadi APPROVED.
     */
    @Transactional
    public RentalResponse approveRental(Long rentalId, String adminUsername) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental tidak ditemukan"));

        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new BadRequestException("Hanya rental dengan status PENDING yang bisa di-approve");
        }

        rental.setStatus(RentalStatus.APPROVED);

        // Update status mobil jika rental mulai hari ini
        if (!rental.getStartDate().isAfter(LocalDate.now())) {
            rental.getCar().setStatus(CarStatus.RENTED);
            carRepository.save(rental.getCar());
            rental.setStatus(RentalStatus.ACTIVE);
        }

        Rental updatedRental = rentalRepository.save(rental);

        activityLogService.log(
                adminUsername, "ADMIN",
                "APPROVE_RENTAL", "Rental", rental.getId(),
                "Rental di-approve: " + rental.getCar().getBrand() + " " + rental.getCar().getModel()
                        + " untuk user: " + rental.getUser().getUsername());

        return mapToResponse(updatedRental);
    }

    /**
     * Admin reject rental.
     */
    @Transactional
    public RentalResponse rejectRental(Long rentalId, String adminUsername) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental tidak ditemukan"));

        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new BadRequestException("Hanya rental dengan status PENDING yang bisa di-reject");
        }

        rental.setStatus(RentalStatus.REJECTED);
        Rental updatedRental = rentalRepository.save(rental);

        activityLogService.log(
                adminUsername, "ADMIN",
                "REJECT_RENTAL", "Rental", rental.getId(),
                "Rental di-reject: " + rental.getCar().getBrand() + " " + rental.getCar().getModel()
                        + " untuk user: " + rental.getUser().getUsername());

        return mapToResponse(updatedRental);
    }

    /**
     * Admin complete rental → mobil kembali jadi AVAILABLE.
     */
    @Transactional
    public RentalResponse completeRental(Long rentalId, String adminUsername) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental tidak ditemukan"));

        if (rental.getStatus() != RentalStatus.APPROVED && rental.getStatus() != RentalStatus.ACTIVE) {
            throw new BadRequestException("Hanya rental APPROVED/ACTIVE yang bisa diselesaikan");
        }

        rental.setStatus(RentalStatus.COMPLETED);
        rental.getCar().setStatus(CarStatus.AVAILABLE);
        carRepository.save(rental.getCar());

        Rental updatedRental = rentalRepository.save(rental);

        activityLogService.log(
                adminUsername, "ADMIN",
                "COMPLETE_RENTAL", "Rental", rental.getId(),
                "Rental diselesaikan: " + rental.getCar().getBrand() + " " + rental.getCar().getModel()
                        + " - user: " + rental.getUser().getUsername());

        return mapToResponse(updatedRental);
    }

    /**
     * User cancel rental sendiri (hanya jika masih PENDING).
     */
    @Transactional
    public RentalResponse cancelRental(Long rentalId, String username) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental tidak ditemukan"));

        // Pastikan user hanya bisa cancel rental miliknya sendiri
        if (!rental.getUser().getUsername().equals(username)) {
            throw new BadRequestException("Anda tidak bisa membatalkan rental milik user lain");
        }

        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new BadRequestException("Hanya rental dengan status PENDING yang bisa dibatalkan");
        }

        rental.setStatus(RentalStatus.CANCELLED);
        Rental updatedRental = rentalRepository.save(rental);

        activityLogService.log(
                username, "USER",
                "CANCEL_RENTAL", "Rental", rental.getId(),
                "Rental dibatalkan oleh user: " + username);

        return mapToResponse(updatedRental);
    }

    /**
     * Ambil riwayat rental user.
     */
    public List<RentalResponse> getUserRentals(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        return rentalRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Ambil semua rental (admin).
     */
    public List<RentalResponse> getAllRentals() {
        return rentalRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mapping entity Rental ke DTO RentalResponse.
     * Include data user dan car agar frontend tidak perlu request tambahan.
     */
    private RentalResponse mapToResponse(Rental rental) {
        return RentalResponse.builder()
                .id(rental.getId())
                .userId(rental.getUser().getId())
                .userName(rental.getUser().getUsername())
                .userFullName(rental.getUser().getFullName())
                .carId(rental.getCar().getId())
                .carBrand(rental.getCar().getBrand())
                .carModel(rental.getCar().getModel())
                .carLicensePlate(rental.getCar().getLicensePlate())
                .startDate(rental.getStartDate())
                .endDate(rental.getEndDate())
                .totalCost(rental.getTotalCost())
                .status(rental.getStatus().name())
                .createdAt(rental.getCreatedAt())
                .updatedAt(rental.getUpdatedAt())
                .build();
    }
}
