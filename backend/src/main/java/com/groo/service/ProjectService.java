package com.groo.service;

import com.groo.common.exception.BusinessException;
import com.groo.common.exception.ErrorCode;
import com.groo.domain.group.Group;
import com.groo.domain.group.GroupMembershipRepository;
import com.groo.domain.group.GroupRepository;
import com.groo.domain.project.Project;
import com.groo.domain.project.ProjectRepository;
import com.groo.domain.user.User;
import com.groo.domain.user.UserRepository;
import com.groo.dto.CreateProjectRequest;
import com.groo.dto.PageResponse;
import com.groo.dto.ProjectResponse;
import com.groo.security.UserPrincipal;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            GroupRepository groupRepository,
            GroupMembershipRepository membershipRepository,
            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectResponse> listByGroup(Long groupId, UserPrincipal principal) {
        Long userId = requirePrincipal(principal);
        requireMembership(groupId, userId);
        return projectRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    public PageResponse<ProjectResponse> searchProjects(
            Long groupId,
            String keyword,
            com.groo.domain.project.ProjectStatus status,
            int page,
            int size,
            String sort,
            UserPrincipal principal) {
        Long userId = requirePrincipal(principal);
        requireMembership(groupId, userId);
        String normalizedKeyword = normalizeKeyword(keyword);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), resolveSize(size), resolveSort(sort));
        Page<Project> projectPage = projectRepository.searchProjects(groupId, status, normalizedKeyword, pageable);
        return PageResponse.from(projectPage.map(ProjectResponse::from));
    }

    public ProjectResponse create(CreateProjectRequest request, UserPrincipal principal) {
        Long userId = requirePrincipal(principal);
        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));
        requireMembership(group.getId(), userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String trimmedName = request.name().trim();
        if (!StringUtils.hasText(trimmedName)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        String description = StringUtils.hasText(request.description()) ? request.description().trim() : null;
        Project project = new Project(group, trimmedName, description, user);
        return ProjectResponse.from(projectRepository.save(project));
    }

    private Long requirePrincipal(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return principal.getId();
    }

    private void requireMembership(Long groupId, Long userId) {
        membershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED));
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    private int resolveSize(int size) {
        return Math.min(Math.max(size, 5), 50);
    }

    private Sort resolveSort(String sort) {
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
            case "name" -> Sort.by(direction, "name");
            case "status" -> Sort.by(direction, "status").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            default -> Sort.by(direction, "createdAt");
        };
    }
}
