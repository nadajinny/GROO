package com.groo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.groo.support.IntegrationTestSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void currentUserRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void currentUserReturnsProfile() throws Exception {
        createUser("profile@example.com", "Password1!");
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "email", "profile@example.com",
                                "password", "Password1!"))))
                .andExpect(status().isOk())
                .andReturn();

        String token = readJson(login).path("data").path("accessToken").asText();

        MvcResult result = mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = readJson(result).path("data");
        assertThat(data.path("email").asText()).isEqualTo("profile@example.com");
        assertThat(data.path("role").asText()).isEqualTo("USER");
    }
}
