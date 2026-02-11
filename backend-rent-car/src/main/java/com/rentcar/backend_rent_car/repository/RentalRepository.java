package com.rentcar.backend_rent_car.repository;

import com.rentcar.backend_rent_car.entity.Rental;
import com.rentcar.backend_rent_car.enums.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Rental> findAllByOrderByCreatedAtDesc();

    /**
     * Check apakah mobil sudah ada rental yang overlap di tanggal tertentu.
     * Digunakan untuk mencegah double booking.
     * 
     * Overlap terjadi jika: startDate <= existingEndDate AND endDate >=
     * existingStartDate
     */
    @Query("SELECT COUNT(r) > 0 FROM Rental r WHERE r.car.id = :carId " +
            "AND r.status NOT IN ('CANCELLED', 'REJECTED', 'COMPLETED') " +
            "AND r.startDate <= :endDate AND r.endDate >= :startDate")
    boolean existsOverlappingRental(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<Rental> findByCarIdAndStatusIn(Long carId, List<RentalStatus> statuses);
}
