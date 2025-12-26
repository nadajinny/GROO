package com.groo.controller;

import com.groo.common.ApiResponse;
import com.groo.dto.AdminUserStatsResponse;
import com.groo.dto.AdminWorkloadStatsResponse;
import com.groo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final UserService userService;

    public AdminStatsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<AdminUserStatsResponse>> userStats() {
        return ResponseEntity.ok(ApiResponse.success(userService.userStats()));
    }

    @GetMapping("/workload")
    public ResponseEntity<ApiResponse<AdminWorkloadStatsResponse>> workloadStats() {
        return ResponseEntity.ok(ApiResponse.success(userService.workloadStats()));
    }
}
