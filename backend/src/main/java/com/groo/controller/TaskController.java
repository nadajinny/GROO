package com.groo.controller;

import com.groo.common.ApiResponse;
import com.groo.dto.AddSubtaskRequest;
import com.groo.dto.CreateTaskRequest;
import com.groo.dto.SubtaskResponse;
import com.groo.dto.TaskActivityResponse;
import com.groo.dto.TaskCommentRequest;
import com.groo.dto.TaskCommentResponse;
import com.groo.dto.TaskResponse;
import com.groo.dto.UpdateTaskRequest;
import com.groo.security.UserPrincipal;
import com.groo.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> listByProject(
            @RequestParam Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(taskService.listByProject(projectId, principal)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(taskService.create(request, principal)));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(
            @PathVariable Long taskId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTask(taskId, principal)));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> update(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateTask(taskId, request, principal)));
    }

    @GetMapping("/{taskId}/subtasks")
    public ResponseEntity<ApiResponse<List<SubtaskResponse>>> listSubtasks(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(taskService.listSubtasks(taskId, principal)));
    }

    @PostMapping("/{taskId}/subtasks")
    public ResponseEntity<ApiResponse<SubtaskResponse>> addSubtask(
            @PathVariable Long taskId,
            @Valid @RequestBody AddSubtaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(taskService.addSubtask(taskId, request, principal)));
    }

    @PatchMapping("/{taskId}/subtasks/{subtaskId}")
    public ResponseEntity<ApiResponse<Void>> toggleSubtask(
            @PathVariable Long taskId,
            @PathVariable Long subtaskId,
            @RequestParam boolean done,
            @AuthenticationPrincipal UserPrincipal principal) {
        taskService.toggleSubtask(taskId, subtaskId, done, principal);
        return ResponseEntity.ok(ApiResponse.success(null, "하위 작업이 업데이트되었습니다."));
    }

    @DeleteMapping("/{taskId}/subtasks/{subtaskId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubtask(
            @PathVariable Long taskId,
            @PathVariable Long subtaskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        taskService.deleteSubtask(taskId, subtaskId, principal);
        return ResponseEntity.ok(ApiResponse.success(null, "하위 작업이 삭제되었습니다."));
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> listComments(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(taskService.listComments(taskId, principal)));
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(taskService.addComment(taskId, request, principal)));
    }

    @GetMapping("/{taskId}/activity")
    public ResponseEntity<ApiResponse<List<TaskActivityResponse>>> activity(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(taskService.listActivity(taskId, principal)));
    }
}
