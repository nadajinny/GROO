package com.groo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.groo.domain.group.Group;
import com.groo.domain.group.GroupRole;
import com.groo.domain.project.Project;
import com.groo.domain.task.Task;
import com.groo.domain.user.User;
import com.groo.support.IntegrationTestSupport;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectTaskControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void createProjectWithinGroupSucceeds() throws Exception {
        User owner = createUser("project-owner@example.com", "Password1!");
        Group group = createGroup(owner, "Project Group");
        String token = obtainAccessToken("project-owner@example.com", "Password1!");

        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(Map.of(
                                "groupId", group.getId(),
                                "name", "My Project",
                                "description", "From tests"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = readJson(result).path("data");
        assertThat(data.path("name").asText()).isEqualTo("My Project");
    }

    @Test
    void createProjectFailsWhenGroupMissing() throws Exception {
        createUser("project-missing@example.com", "Password1!");
        String token = obtainAccessToken("project-missing@example.com", "Password1!");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(Map.of(
                                "groupId", 9999,
                                "name", "Ghost Project",
                                "description", "none"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listProjectsDeniedForNonMember() throws Exception {
        User owner = createUser("project-owner2@example.com", "Password1!");
        Group group = createGroup(owner, "Closed Group");
        String ownerToken = obtainAccessToken("project-owner2@example.com", "Password1!");
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + ownerToken)
                        .content(toJson(Map.of(
                                "groupId", group.getId(),
                                "name", "Secret Project",
                                "description", "hidden"))))
                .andExpect(status().isOk());

        createUser("outsider-project@example.com", "Password1!");
        String outsiderToken = obtainAccessToken("outsider-project@example.com", "Password1!");

        mockMvc.perform(get("/api/projects")
                        .param("groupId", String.valueOf(group.getId()))
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTaskWithinProjectSucceeds() throws Exception {
        User owner = createUser("task-owner@example.com", "Password1!");
        Group group = createGroup(owner, "Task Group");
        Project project = createProject(group, owner, "Task Project");
        String token = obtainAccessToken("task-owner@example.com", "Password1!");

        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(Map.of(
                                "projectId", project.getId(),
                                "title", "Implement feature",
                                "description", "task from tests",
                                "assigneeId", "tester",
                                "dueDate", Instant.now().toString(),
                                "status", "TODO",
                                "priority", "HIGH"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = readJson(result).path("data");
        assertThat(data.path("title").asText()).isEqualTo("Implement feature");
    }

    @Test
    void updateTaskWithoutChangesFails() throws Exception {
        User owner = createUser("task-update@example.com", "Password1!");
        Group group = createGroup(owner, "Update Group");
        Project project = createProject(group, owner, "Update Project");
        Task task = createTask(project, owner, "Existing Task");
        String token = obtainAccessToken("task-update@example.com", "Password1!");

        mockMvc.perform(patch("/api/tasks/{taskId}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addSubtaskRequiresTitle() throws Exception {
        User owner = createUser("subtask@example.com", "Password1!");
        Group group = createGroup(owner, "Subtask Group");
        Project project = createProject(group, owner, "Subtask Project");
        Task task = createTask(project, owner, "Parent Task");
        String token = obtainAccessToken("subtask@example.com", "Password1!");

        mockMvc.perform(post("/api/tasks/{taskId}/subtasks", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(Map.of("title", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listTasksDeniedForNonMember() throws Exception {
        User owner = createUser("task-owner3@example.com", "Password1!");
        Group group = createGroup(owner, "Owner Group");
        Project project = createProject(group, owner, "Owner Project");
        createTask(project, owner, "Owner Task");
        User outsider = createUser("task-outsider@example.com", "Password1!");
        String outsiderToken = obtainAccessToken("task-outsider@example.com", "Password1!");

        mockMvc.perform(get("/api/tasks")
                        .param("projectId", String.valueOf(project.getId()))
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());

        addMember(group, outsider, GroupRole.MEMBER);
        mockMvc.perform(get("/api/tasks")
                        .param("projectId", String.valueOf(project.getId()))
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isOk());
    }
}
