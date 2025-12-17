package com.groo.controller;

import com.groo.common.ApiResponse;
import com.groo.dto.CreateGroupRequest;
import com.groo.dto.GroupDto;
import com.groo.service.GroupService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupDto>>> findAll() {
        List<GroupDto> groups = groupService.findAll();
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupDto>> create(@Valid @RequestBody CreateGroupRequest request) {
        GroupDto group = groupService.create(request.name(), request.description());
        return ResponseEntity.ok(ApiResponse.success(group));
    }
}
