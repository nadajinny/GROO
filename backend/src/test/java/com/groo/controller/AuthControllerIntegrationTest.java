package com.groo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.groo.domain.user.User;
import com.groo.support.IntegrationTestSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void registerCreatesUserAndReturnsTokens() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "email", "new.user@example.com",
                                "password", "Str0ngPass!",
                                "displayName", "New User"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = readJson(result);
        assertThat(root.path("success").asBoolean()).isTrue();
        assertThat(root.path("data").path("accessToken").asText()).isNotBlank();
        assertThat(userRepository.findByEmail("new.user@example.com")).isPresent();
    }

    @Test
    void registerDuplicateEmailReturnsConflict() throws Exception {
        createUser("dup@example.com", "Password1!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "email", "dup@example.com",
                                "password", "OtherPass1!",
                                "displayName", "Dup User"))))
                .andExpect(status().isConflict());
    }

    @Test
    void loginReturnsTokensForValidCredentials() throws Exception {
        createUser("login@example.com", "Password1!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "email", "login@example.com",
                                "password", "Password1!"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = readJson(result).path("data");
        assertThat(data.path("accessToken").asText()).isNotBlank();
        assertThat(data.path("refreshToken").asText()).isNotBlank();
    }

    @Test
    void loginFailsWhenPasswordMismatch() throws Exception {
        createUser("wrongpass@example.com", "Password1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "email", "wrongpass@example.com",
                                "password", "InvalidPass!"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginFailsWhenUserDeactivated() throws Exception {
        User user = createUser("inactive@example.com", "Password1!");
        user.deactivate();
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "email", "inactive@example.com",
                                "password", "Password1!"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshIssuesNewAccessToken() throws Exception {
        User user = createUser("refresh@example.com", "Password1!");
        String refreshToken = issueRefreshTokenFor(user);

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = readJson(result).path("data");
        assertThat(data.path("accessToken").asText()).isNotBlank();
        assertThat(data.path("refreshToken").asText()).isNotBlank();
    }

    @Test
    void refreshFailsWhenTokenUnknown() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("refreshToken", "invalid-token"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        createUser("logout@example.com", "Password1!");
        Map<String, String> tokens = obtainAuthTokens("logout@example.com", "Password1!");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokens.get("accessToken"))
                        .content(toJson(Map.of("refreshToken", tokens.get("refreshToken")))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("refreshToken", tokens.get("refreshToken")))))
                .andExpect(status().isUnauthorized());
    }
}
