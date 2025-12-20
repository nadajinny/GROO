package com.groo.controller;

import com.groo.common.ApiResponse;
import com.groo.domain.group.GroupStatus;
import com.groo.dto.AddGroupMemberRequest;
import com.groo.dto.CreateGroupRequest;
import com.groo.dto.GroupDetailDto;
import com.groo.dto.GroupMemberDto;
import com.groo.dto.GroupSummaryDto;
import com.groo.dto.JoinGroupRequest;
import com.groo.dto.PageResponse;
import com.groo.dto.UpdateGroupRequest;
import com.groo.security.UserPrincipal;
import com.groo.service.GroupService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupSummaryDto>>> myGroups(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(groupService.myGroups(principal)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<GroupSummaryDto>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) GroupStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(groupService.searchGroups(keyword, status, page, size, principal)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupDetailDto>> create(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(groupService.createGroup(request, principal)));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDetailDto>> get(
            @PathVariable Long groupId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getGroup(groupId, principal)));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupDetailDto>> update(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(groupService.updateGroup(groupId, request, principal)));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberDto>>> members(
            @PathVariable Long groupId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(groupService.listMembers(groupId, principal)));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<GroupMemberDto>> addMember(
            @PathVariable Long groupId,
            @Valid @RequestBody AddGroupMemberRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(groupService.addMember(groupId, request, principal)));
    }

    @DeleteMapping("/{groupId}/members/{membershipId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long membershipId,
            @AuthenticationPrincipal UserPrincipal principal) {
        groupService.removeMember(groupId, membershipId, principal);
        return ResponseEntity.ok(ApiResponse.success(null, "멤버가 삭제되었습니다."));
    }

    @PostMapping("/{groupId}/invites")
    public ResponseEntity<ApiResponse<String>> regenerateCode(
            @PathVariable Long groupId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(groupService.regenerateInvitation(groupId, principal)));
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<GroupDetailDto>> join(
            @Valid @RequestBody JoinGroupRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(groupService.joinByInvitation(request, principal)));
    }
}
