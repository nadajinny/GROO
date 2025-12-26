package com.groo.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groo.domain.auth.RefreshToken;
import com.groo.domain.auth.RefreshTokenRepository;
import com.groo.domain.group.Group;
import com.groo.domain.group.GroupMembership;
import com.groo.domain.group.GroupMembershipRepository;
import com.groo.domain.group.GroupRepository;
import com.groo.domain.group.GroupRole;
import com.groo.domain.project.Project;
import com.groo.domain.project.ProjectRepository;
import com.groo.domain.task.Task;
import com.groo.domain.task.TaskActivityRepository;
import com.groo.domain.task.TaskCommentRepository;
import com.groo.domain.task.TaskRepository;
import com.groo.domain.task.TaskSubtaskRepository;
import com.groo.domain.user.Role;
import com.groo.domain.user.User;
import com.groo.domain.user.UserRepository;
import com.groo.security.JwtTokenProvider;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    protected GroupMembershipRepository groupMembershipRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected TaskSubtaskRepository taskSubtaskRepository;

    @Autowired
    protected TaskCommentRepository taskCommentRepository;

    @Autowired
    protected TaskActivityRepository taskActivityRepository;

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    protected static final MediaType JSON = MediaType.APPLICATION_JSON;

    @AfterEach
    void cleanDatabase() {
        taskActivityRepository.deleteAll();
        taskCommentRepository.deleteAll();
        taskSubtaskRepository.deleteAll();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        groupMembershipRepository.deleteAll();
        groupRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User createUser(String email, String rawPassword) {
        User user = new User(email, passwordEncoder.encode(rawPassword), "Tester");
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    protected Group createGroup(User owner, String name) {
        Group group = new Group(name, "description", owner);
        groupRepository.save(group);
        groupMembershipRepository.save(new GroupMembership(group, owner, GroupRole.OWNER));
        return group;
    }

    protected void addMember(Group group, User user, GroupRole role) {
        groupMembershipRepository.save(new GroupMembership(group, user, role));
    }

    protected Project createProject(Group group, User creator, String name) {
        Project project = new Project(group, name, "description", creator);
        return projectRepository.save(project);
    }

    protected Task createTask(Project project, User creator, String title) {
        Task task = new Task(project, title, "details", creator);
        return taskRepository.save(task);
    }

    protected String obtainAccessToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(JSON)
                        .content(toJson(Map.of(
                                "email", email,
                                "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = readJson(result).path("data");
        return data.path("accessToken").asText();
    }

    protected Map<String, String> obtainAuthTokens(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(JSON)
                        .content(toJson(Map.of(
                                "email", email,
                                "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = readJson(result).path("data");
        return Map.of(
                "accessToken", data.path("accessToken").asText(),
                "refreshToken", data.path("refreshToken").asText());
    }

    protected String issueRefreshTokenFor(User user) {
        String refreshToken = "test-refresh-" + UUID.randomUUID();
        refreshTokenRepository.save(new RefreshToken(
                user,
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiryInstant()));
        return refreshToken;
    }

    protected String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
