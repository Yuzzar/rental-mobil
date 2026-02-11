package com.rentcar.backend_rent_car.service;

import com.rentcar.backend_rent_car.entity.ActivityLog;
import com.rentcar.backend_rent_car.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service untuk Activity Logging.
 * Mencatat semua aktivitas yang dilakukan oleh admin dan user
 * untuk keperluan audit trail.
 */
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    /**
     * Simpan log aktivitas ke database.
     */
    public void log(String username, String role, String action,
            String entityName, Long entityId, String details) {
        ActivityLog activityLog = ActivityLog.builder()
                .username(username)
                .role(role)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .build();
        activityLogRepository.save(activityLog);
    }

    /**
     * Ambil semua activity log, diurutkan dari yang terbaru.
     */
    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAllByOrderByTimestampDesc();
    }

    /**
     * Ambil activity log berdasarkan username.
     */
    public List<ActivityLog> getLogsByUsername(String username) {
        return activityLogRepository.findByUsernameOrderByTimestampDesc(username);
    }
}
