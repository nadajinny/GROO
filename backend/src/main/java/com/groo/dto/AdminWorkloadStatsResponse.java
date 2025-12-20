package com.groo.dto;

public record AdminWorkloadStatsResponse(
        long totalGroups,
        long activeGroups,
        long archivedGroups,
        long totalProjects,
        long activeProjects,
        long archivedProjects,
        long totalTasks,
        long todoTasks,
        long doingTasks,
        long doneTasks) {}
