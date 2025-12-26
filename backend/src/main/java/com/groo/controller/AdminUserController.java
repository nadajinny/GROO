package com.groo.controller;

import com.groo.common.ApiResponse;
import com.groo.domain.user.Role;
import com.groo.domain.user.UserStatus;
import com.groo.dto.DeactivateUserRequest;
import com.groo.dto.PageResponse;
import com.groo.dto.UpdateUserRoleRequest;
import com.groo.dto.UserResponse;
import com.groo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,DESC") String sort) {
        return ResponseEntity.ok(ApiResponse.success(userService.searchUsers(keyword, role, status, page, size, sort)));
    }

    @PatchMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateRole(id, request), "권한이 변경되었습니다."));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivate(
            @PathVariable Long id, @Valid @RequestBody DeactivateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.deactivate(id, request), "사용자가 비활성화되었습니다."));
    }
}
