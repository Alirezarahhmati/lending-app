package com.lending.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lending.app.Application;
import com.lending.app.model.entity.User;
import com.lending.app.model.enums.Role;
import com.lending.app.model.record.base.BaseResponse;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;
import com.lending.app.repository.UserRepository;
import com.lending.app.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Import({NoOpCacheManager.class})
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String testUserId;
    private UpdateUserCommand updateCommand;

    @BeforeEach
    void setupUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setEmail("test@example.com");
        user.setScore(10);
        user.setRole(Role.USER);
        user = userRepository.save(user);
        testUserId = user.getId();

        updateCommand = new UpdateUserCommand(
                "update",
                "newpassword123",
                "updated@example.com"
        );
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {
        @Test
        @WithMockUser(username = "test")
        void shouldGetCurrentUser() throws Exception {
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

                MvcResult result = mockMvc.perform(get("/api/users"))
                        .andExpect(status().isOk())
                        .andReturn();

                BaseResponse<UserMessage> response = objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {}
                );

                UserMessage user = response.result();
                assertThat(user).isNotNull();
                assertThat(user.username()).isEqualTo("test");
                assertThat(user.email()).isEqualTo("test@example.com");
            }
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {
        @Test
        @WithMockUser(username = "test")
        void shouldUpdateUserSuccessfully() throws Exception {
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

                MvcResult result = mockMvc.perform(put("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateCommand)))
                        .andExpect(status().isOk())
                        .andReturn();

                BaseResponse<UserMessage> response = objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {}
                );

                UserMessage updated = response.result();
                assertThat(updated).isNotNull();
                assertThat(updated.username()).isEqualTo(updateCommand.username());
                assertThat(updated.email()).isEqualTo(updateCommand.email());
            }
        }
    }

    @Nested
    @DisplayName("Unauthorized Access Tests")
    class UnauthorizedTests {
        @Test
        void shouldReturnUnauthorizedForGet() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturnUnauthorizedForUpdate() throws Exception {
            mockMvc.perform(put("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateCommand)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturnUnauthorizedForDelete() throws Exception {
            mockMvc.perform(delete("/api/users"))
                    .andExpect(status().isUnauthorized());
        }
    }
}