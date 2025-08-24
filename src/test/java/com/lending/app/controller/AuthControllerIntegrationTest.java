package com.lending.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lending.app.Application;
import com.lending.app.model.record.auth.AuthMessage;
import com.lending.app.model.record.auth.SignInCommand;
import com.lending.app.model.record.auth.SignUpCommand;
import com.lending.app.model.record.base.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Import({NoOpCacheManager.class})
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private SignUpCommand validSignUp;
    private SignInCommand validSignIn;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();
        validSignUp = new SignUpCommand(
                "testuser" + timestamp,
                "password123",
                "testuser" + timestamp + "@example.com"
        );
        validSignIn = new SignInCommand(validSignUp.username(), validSignUp.password());
    }

    @Nested
    @DisplayName("SignUp Tests")
    class SignUpTests {

        @Test
        void shouldSignUpSuccessfully() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validSignUp)))
                    .andExpect(status().isOk())
                    .andReturn();

            BaseResponse<AuthMessage> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            assertThat(response.result()).isNotNull();
            assertThat(response.result().token()).isNotBlank();
        }

        @Test
        void shouldFailSignUpIfUsernameExists() throws Exception {
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validSignUp)))
                    .andExpect(status().isOk());

            SignUpCommand duplicateUsername = new SignUpCommand(
                    validSignUp.username(),
                    "newpassword",
                    "otheremail@example.com"
            );

            MvcResult result = mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateUsername)))
                    .andExpect(status().isConflict())
                    .andReturn();

            BaseResponse<?> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            assertThat(response.message()).contains("already exists");
        }

        @Test
        void shouldFailSignUpValidation() throws Exception {
            SignUpCommand invalid = new SignUpCommand("test", "123456", "invalid-email");

            MvcResult result = mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            BaseResponse<?> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            assertThat(response.message()).contains("email");
        }
    }

    @Nested
    @DisplayName("SignIn Tests")
    class SignInTests {

        @BeforeEach
        void registerUser() throws Exception {
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validSignUp)))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldSignInSuccessfully() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validSignIn)))
                    .andExpect(status().isOk())
                    .andReturn();

            BaseResponse<AuthMessage> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            assertThat(response.result()).isNotNull();
            assertThat(response.result().token()).isNotBlank();
        }

        @Test
        void shouldFailSignInWithWrongPassword() throws Exception {
            SignInCommand wrongPassword = new SignInCommand(validSignIn.username(), "wrongpass");

            mockMvc.perform(post("/api/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongPassword)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldFailSignInValidation() throws Exception {
            SignInCommand invalid = new SignInCommand("", "123456");

            MvcResult result = mockMvc.perform(post("/api/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            BaseResponse<?> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            assertThat(response.message()).contains("username");
        }
    }
}