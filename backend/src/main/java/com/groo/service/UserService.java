package com.groo.service;

import com.groo.common.exception.BusinessException;
import com.groo.common.exception.ErrorCode;
import com.groo.domain.group.GroupRepository;
import com.groo.domain.group.GroupStatus;
import com.groo.domain.project.ProjectRepository;
import com.groo.domain.project.ProjectStatus;
import com.groo.domain.task.TaskRepository;
import com.groo.domain.task.TaskStatus;
import com.groo.domain.user.Role;
import com.groo.domain.user.User;
import com.groo.domain.user.UserRepository;
import com.groo.domain.user.UserStatus;
import com.groo.dto.AdminUserStatsResponse;
import com.groo.dto.AdminWorkloadStatsResponse;
import com.groo.dto.DeactivateUserRequest;
import com.groo.dto.PageResponse;
import com.groo.dto.UpdateUserRoleRequest;
import com.groo.dto.UserResponse;
import com.groo.security.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public UserService(
            UserRepository userRepository,
            GroupRepository groupRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public UserResponse getCurrentUser(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    public UserResponse updateRole(Long id, UpdateUserRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setRole(request.role());
        return UserResponse.from(user);
    }

    public UserResponse deactivate(Long id, DeactivateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.deactivate();
        return UserResponse.from(user);
    }

    public PageResponse<UserResponse> searchUsers(
            String keyword, Role role, UserStatus status, int page, int size, String sort) {
        String normalizedKeyword = normalizeKeyword(keyword);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), resolveUserPageSize(size), resolveUserSort(sort));
        Page<User> result = userRepository.searchUsers(normalizedKeyword, role, status, pageable);
        return PageResponse.from(result.map(UserResponse::from));
    }

    public AdminUserStatsResponse userStats() {
        long total = userRepository.count();
        long active = userRepository.countByStatus(UserStatus.ACTIVE);
        long deactivated = userRepository.countByStatus(UserStatus.DEACTIVATED);
        long admins = userRepository.countByRole(Role.ADMIN);
        return new AdminUserStatsResponse(total, active, deactivated, admins);
    }

    public AdminWorkloadStatsResponse workloadStats() {
        long totalGroups = groupRepository.count();
        long activeGroups = groupRepository.countByStatus(GroupStatus.ACTIVE);
        long archivedGroups = groupRepository.countByStatus(GroupStatus.ARCHIVED);
        long totalProjects = projectRepository.count();
        long activeProjects = projectRepository.countByStatus(ProjectStatus.ACTIVE);
        long archivedProjects = projectRepository.countByStatus(ProjectStatus.ARCHIVED);
        long totalTasks = taskRepository.count();
        long todoTasks = taskRepository.countByStatus(TaskStatus.TODO);
        long doingTasks = taskRepository.countByStatus(TaskStatus.DOING);
        long doneTasks = taskRepository.countByStatus(TaskStatus.DONE);
        return new AdminWorkloadStatsResponse(
                totalGroups,
                activeGroups,
                archivedGroups,
                totalProjects,
                activeProjects,
                archivedProjects,
                totalTasks,
                todoTasks,
                doingTasks,
                doneTasks);
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    private int resolveUserPageSize(int size) {
        return Math.min(Math.max(size, 5), 100);
    }

    private Sort resolveUserSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] tokens = sort.split(",");
        String field = tokens[0].trim();
        Sort.Direction direction = Sort.Direction.DESC;
        if (tokens.length > 1) {
            direction = Sort.Direction.fromOptionalString(tokens[1].trim().toUpperCase())
                    .orElse(Sort.Direction.DESC);
        }
        return switch (field) {
            case "displayName" -> Sort.by(direction, "displayName");
            case "email" -> Sort.by(direction, "email");
            case "role" -> Sort.by(direction, "role").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            default -> Sort.by(direction, "createdAt");
        };
    }
}
