package com.rentcar.backend_rent_car.repository;

import com.rentcar.backend_rent_car.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByOrderByTimestampDesc();

    List<ActivityLog> findByUsernameOrderByTimestampDesc(String username);
}
