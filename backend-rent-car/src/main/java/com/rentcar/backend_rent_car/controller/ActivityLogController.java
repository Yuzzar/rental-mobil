package com.rentcar.backend_rent_car.controller;

import com.rentcar.backend_rent_car.dto.ApiResponse;
import com.rentcar.backend_rent_car.entity.ActivityLog;
import com.rentcar.backend_rent_car.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Activity Log Controller - admin only.
 * Melihat semua activity log untuk audit trail.
 */
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityLog>>> getAllLogs() {
        List<ActivityLog> logs = activityLogService.getAllLogs();
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
