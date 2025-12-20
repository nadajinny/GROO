package com.groo.dto;

public record AdminUserStatsResponse(
        long totalUsers,
        long activeUsers,
        long deactivatedUsers,
        long adminUsers) {}
