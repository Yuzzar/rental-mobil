package com.rentcar.backend_rent_car.repository;

import com.rentcar.backend_rent_car.entity.Car;
import com.rentcar.backend_rent_car.enums.CarStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {

    List<Car> findByStatus(CarStatus status);

    List<Car> findByBrandContainingIgnoreCase(String brand);

    /**
     * Pessimistic lock: mencegah 2 user rental mobil yang sama secara bersamaan.
     * Ketika query ini dijalankan, row mobil tersebut di-lock di database
     * sehingga transaksi lain harus menunggu sampai lock dilepas.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Car c WHERE c.id = :id")
    Optional<Car> findByIdWithLock(@Param("id") Long id);
}
