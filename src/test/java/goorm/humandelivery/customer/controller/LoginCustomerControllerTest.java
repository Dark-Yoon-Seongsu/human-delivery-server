package goorm.humandelivery.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.humandelivery.customer.application.RegisterCustomerService;
import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
import goorm.humandelivery.customer.dto.request.LoginCustomerRequest;
import goorm.humandelivery.customer.dto.request.RegisterCustomerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginCustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SaveCustomerPort saveCustomerPort;
    
    @Autowired
    private RegisterCustomerService registerCustomerService;

    @AfterEach
    void tearDown() {
        saveCustomerPort.deleteAllInBatch();
    }

    @Nested
    @DisplayName("POST /api/v1/customer/auth-tokens - 로그인 API 테스트")
    class LoginCustomerApiTest {

        @Test
        @DisplayName("정상적인 로그인 요청이 성공한다.")
        void login_Success() throws Exception {
            // Given - 회원가입 먼저 진행
            RegisterCustomerRequest registerRequest = new RegisterCustomerRequest(
                "testuser", "password123", "홍길동", "010-1234-5678"
            );
            registerCustomerService.register(registerRequest);

            LoginCustomerRequest loginRequest = new LoginCustomerRequest("testuser", "password123");

            // When & Then
            mockMvc.perform(post("/api/v1/customer/auth-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty());
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인하면 404 에러가 발생한다.")
        void login_UserNotFound_ReturnsNotFound() throws Exception {
            // Given
            LoginCustomerRequest loginRequest = new LoginCustomerRequest("nonexistent", "password123");

            // When & Then
            mockMvc.perform(post("/api/v1/customer/auth-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인하면 400 에러가 발생한다.")
        void login_WrongPassword_ReturnsBadRequest() throws Exception {
            // Given - 회원가입 먼저 진행
            RegisterCustomerRequest registerRequest = new RegisterCustomerRequest(
                "testuser", "password123", "홍길동", "010-1234-5678"
            );
            registerCustomerService.register(registerRequest);

            LoginCustomerRequest loginRequest = new LoginCustomerRequest("testuser", "wrongpassword");

            // When & Then
            mockMvc.perform(post("/api/v1/customer/auth-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400 에러가 발생한다.")
        void login_MissingRequiredFields_ReturnsBadRequest() throws Exception {
            // Given - loginId가 누락된 요청
            String invalidJson = """
                {
                    "password": "password123"
                }
                """;

            // When & Then
            mockMvc.perform(post("/api/v1/customer/auth-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("빈 요청 본문으로 요청하면 400 에러가 발생한다.")
        void login_EmptyRequestBody_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/customer/auth-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
} 