package com.groo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.groo.domain.group.Group;
import com.groo.domain.group.GroupRole;
import com.groo.domain.user.User;
import com.groo.support.IntegrationTestSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GroupControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void createGroupPersistsOwnerMembership() throws Exception {
        createUser("group-owner@example.com", "Password1!");
        String token = obtainAccessToken("group-owner@example.com", "Password1!");

        MvcResult result = mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(Map.of(
                                "name", "My Test Group",
                                "description", "Demo group"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = readJson(result).path("data");
        assertThat(data.path("name").asText()).isEqualTo("My Test Group");
        assertThat(data.path("members").isArray()).isTrue();
        assertThat(data.path("members").size()).isEqualTo(1);
    }

    @Test
    void listMyGroupsReturnsCreatedGroup() throws Exception {
        User owner = createUser("group-list@example.com", "Password1!");
        String token = obtainAccessToken("group-list@example.com", "Password1!");
        createGroup(owner, "Existing Group");

        MvcResult result = mockMvc.perform(get("/api/groups")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = readJson(result).path("data");
        assertThat(data.isArray()).isTrue();
        assertThat(data.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void getGroupDeniedForNonMember() throws Exception {
        User owner = createUser("group-owner2@example.com", "Password1!");
        Group group = createGroup(owner, "Hidden Group");
        createUser("outsider@example.com", "Password1!");
        String outsiderToken = obtainAccessToken("outsider@example.com", "Password1!");

        mockMvc.perform(get("/api/groups/{id}", group.getId())
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCannotUpdateGroup() throws Exception {
        User owner = createUser("manager@example.com", "Password1!");
        Group group = createGroup(owner, "Editable Group");
        User member = createUser("member@example.com", "Password1!");
        addMember(group, member, GroupRole.MEMBER);
        String memberToken = obtainAccessToken("member@example.com", "Password1!");

        mockMvc.perform(put("/api/groups/{id}", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + memberToken)
                        .content(toJson(Map.of(
                                "name", "Updated Name",
                                "description", "Changed",
                                "archived", false))))
                .andExpect(status().isForbidden());
    }

    @Test
    void joinWithInvalidInvitationCodeFails() throws Exception {
        createUser("joiner@example.com", "Password1!");
        String token = obtainAccessToken("joiner@example.com", "Password1!");

        mockMvc.perform(post("/api/groups/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(toJson(Map.of("invitationCode", "INVALIDCODE"))))
                .andExpect(status().isBadRequest());
    }
}
