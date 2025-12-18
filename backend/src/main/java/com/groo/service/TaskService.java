package com.groo.service;

import com.groo.common.exception.BusinessException;
import com.groo.common.exception.ErrorCode;
import com.groo.domain.group.GroupMembershipRepository;
import com.groo.domain.project.Project;
import com.groo.domain.project.ProjectRepository;
import com.groo.domain.task.Task;
import com.groo.domain.task.TaskActivity;
import com.groo.domain.task.TaskActivityRepository;
import com.groo.domain.task.TaskComment;
import com.groo.domain.task.TaskCommentRepository;
import com.groo.domain.task.TaskRepository;
import com.groo.domain.task.TaskSubtask;
import com.groo.domain.task.TaskSubtaskRepository;
import com.groo.domain.user.User;
import com.groo.domain.user.UserRepository;
import com.groo.dto.AddSubtaskRequest;
import com.groo.dto.CreateTaskRequest;
import com.groo.dto.SubtaskResponse;
import com.groo.dto.TaskActivityResponse;
import com.groo.dto.TaskCommentRequest;
import com.groo.dto.TaskCommentResponse;
import com.groo.dto.TaskResponse;
import com.groo.dto.UpdateTaskRequest;
import com.groo.security.UserPrincipal;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskSubtaskRepository subtaskRepository;
    private final TaskCommentRepository commentRepository;
    private final TaskActivityRepository activityRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            TaskSubtaskRepository subtaskRepository,
            TaskCommentRepository commentRepository,
            TaskActivityRepository activityRepository,
            GroupMembershipRepository membershipRepository,
            UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.subtaskRepository = subtaskRepository;
        this.commentRepository = commentRepository;
        this.activityRepository = activityRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    public List<TaskResponse> listByProject(Long projectId, UserPrincipal principal) {
        Long userId = requirePrincipal(principal);
        Project project = requireProjectAccess(projectId, userId);
        return taskRepository.findByProjectIdOrderByDueDateAsc(project.getId()).stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse create(CreateTaskRequest request, UserPrincipal principal) {
        Long userId = requirePrincipal(principal);
        Project project = requireProjectAccess(request.projectId(), userId);
        User creator = fetchUser(userId);
        if (!StringUtils.hasText(request.title())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        String description = StringUtils.hasText(request.description()) ? request.description().trim() : null;
        Task task = new Task(project, request.title().trim(), description, creator);
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (StringUtils.hasText(request.assigneeId())) {
            task.setAssigneeId(request.assigneeId().trim());
        }
        task.setDueDate(request.dueDate());
        Task saved = taskRepository.save(task);
        logActivity(saved, creator, "작업이 생성되었습니다.");
        return TaskResponse.from(saved);
    }

    public TaskResponse getTask(Long taskId, UserPrincipal principal) {
        Task task = requireTaskAccess(taskId, requirePrincipal(principal));
        return TaskResponse.from(task);
    }

    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, UserPrincipal principal) {
        Long userId = requirePrincipal(principal);
        User actor = fetchUser(userId);
        Task task = requireTaskAccess(taskId, userId);
        boolean changed = false;

        if (request.status() != null && request.status() != task.getStatus()) {
            task.setStatus(request.status());
            logActivity(task, actor, "상태가 " + request.status().name() + " 로 변경되었습니다.");
            changed = true;
        }
        if (request.priority() != null && request.priority() != task.getPriority()) {
            task.setPriority(request.priority());
            logActivity(task, actor, "우선순위가 " + request.priority().name() + " 로 변경되었습니다.");
            changed = true;
        }
        if (Boolean.TRUE.equals(request.updateAssignee())) {
            String nextAssignee = StringUtils.hasText(request.assigneeId())
                    ? request.assigneeId().trim()
                    : null;
            if ((nextAssignee == null && task.getAssigneeId() != null)
                    || (nextAssignee != null && !nextAssignee.equals(task.getAssigneeId()))) {
                task.setAssigneeId(nextAssignee);
                logActivity(task, actor, nextAssignee == null ? "담당자가 해제되었습니다." : "담당자가 변경되었습니다.");
                changed = true;
            }
        }
        if (Boolean.TRUE.equals(request.updateDueDate())) {
            Instant nextDueDate = request.dueDate();
            Instant previous = task.getDueDate();
            if ((nextDueDate == null && previous != null)
                    || (nextDueDate != null && !nextDueDate.equals(previous))) {
                task.setDueDate(nextDueDate);
                logActivity(task, actor, nextDueDate == null ? "마감일이 제거되었습니다." : "마감일이 변경되었습니다.");
                changed = true;
            }
        }
        if (!changed) {
            throw new BusinessException(ErrorCode.TASK_UPDATE_INVALID);
        }
        return TaskResponse.from(task);
    }

    public List<SubtaskResponse> listSubtasks(Long taskId, UserPrincipal principal) {
        requireTaskAccess(taskId, requirePrincipal(principal));
        return subtaskRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(SubtaskResponse::from)
                .toList();
    }

    public SubtaskResponse addSubtask(Long taskId, AddSubtaskRequest request, UserPrincipal principal) {
        User actor = fetchUser(requirePrincipal(principal));
        Task task = requireTaskAccess(taskId, actor.getId());
        if (!StringUtils.hasText(request.title())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        TaskSubtask subtask = subtaskRepository.save(new TaskSubtask(task, request.title().trim()));
        logActivity(task, actor, "하위 작업이 추가되었습니다.");
        return SubtaskResponse.from(subtask);
    }

    public void toggleSubtask(Long taskId, Long subtaskId, boolean done, UserPrincipal principal) {
        requireTaskAccess(taskId, requirePrincipal(principal));
        TaskSubtask subtask = subtaskRepository.findById(subtaskId)
                .filter(item -> item.getTask().getId().equals(taskId))
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBTASK_NOT_FOUND));
        subtask.setDone(done);
    }

    public void deleteSubtask(Long taskId, Long subtaskId, UserPrincipal principal) {
        requireTaskAccess(taskId, requirePrincipal(principal));
        TaskSubtask subtask = subtaskRepository.findById(subtaskId)
                .filter(item -> item.getTask().getId().equals(taskId))
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBTASK_NOT_FOUND));
        subtaskRepository.delete(subtask);
    }

    public List<TaskCommentResponse> listComments(Long taskId, UserPrincipal principal) {
        requireTaskAccess(taskId, requirePrincipal(principal));
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(TaskCommentResponse::from)
                .toList();
    }

    public TaskCommentResponse addComment(Long taskId, TaskCommentRequest request, UserPrincipal principal) {
        User actor = fetchUser(requirePrincipal(principal));
        Task task = requireTaskAccess(taskId, actor.getId());
        if (!StringUtils.hasText(request.content())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        TaskComment comment = commentRepository.save(new TaskComment(task, actor, request.content().trim()));
        logActivity(task, actor, "댓글을 남겼습니다.");
        return TaskCommentResponse.from(comment);
    }

    public List<TaskActivityResponse> listActivity(Long taskId, UserPrincipal principal) {
        requireTaskAccess(taskId, requirePrincipal(principal));
        return activityRepository.findTop50ByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(TaskActivityResponse::from)
                .toList();
    }

    private Task requireTaskAccess(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        requireMembership(task.getProject().getGroup().getId(), userId);
        return task;
    }

    private Project requireProjectAccess(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        requireMembership(project.getGroup().getId(), userId);
        return project;
    }

    private void requireMembership(Long groupId, Long userId) {
        membershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED));
    }

    private Long requirePrincipal(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return principal.getId();
    }

    private User fetchUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void logActivity(Task task, User actor, String message) {
        activityRepository.save(new TaskActivity(task, actor, message));
    }
}
