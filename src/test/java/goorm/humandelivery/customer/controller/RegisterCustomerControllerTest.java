package goorm.humandelivery.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
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
public class RegisterCustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SaveCustomerPort saveCustomerPort;

    @AfterEach
    void tearDown() {
        saveCustomerPort.deleteAllInBatch();
    }

    @Nested
    @DisplayName("POST /api/v1/customer - 회원가입 API 테스트")
    class RegisterCustomerApiTest {

        @Test
        @DisplayName("정상적인 회원가입 요청이 성공한다.")
        void register_Success() throws Exception {
            // Given
            RegisterCustomerRequest request = new RegisterCustomerRequest(
                "testuser@test.com", "password123", "홍길동", "010-1234-5678"
            );

            // When & Then
            mockMvc.perform(post("/api/v1/customer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.loginId").value("testuser@test.com"));
        }

        @Test
        @DisplayName("중복된 로그인 아이디로 회원가입하면 409 에러가 발생한다.")
        void register_DuplicateLoginId_ReturnsConflict() throws Exception {
            // Given
            RegisterCustomerRequest firstRequest = new RegisterCustomerRequest(
                "testuser@test.com", "password123", "홍길동", "010-1234-5678"
            );
            
            // 첫 번째 회원가입
            mockMvc.perform(post("/api/v1/customer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            RegisterCustomerRequest duplicateRequest = new RegisterCustomerRequest(
                "testuser@test.com", "password456", "김철수", "010-9876-5432"
            );

            // When & Then
            mockMvc.perform(post("/api/v1/customer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("중복된 전화번호로 회원가입하면 409 에러가 발생한다.")
        void register_DuplicatePhoneNumber_ReturnsBadRequest() throws Exception {
            // Given
            RegisterCustomerRequest firstRequest = new RegisterCustomerRequest(
                "testuser1@test.com", "password123", "홍길동", "010-1234-5678"
            );
            
            // 첫 번째 회원가입
            mockMvc.perform(post("/api/v1/customer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isOk());

            RegisterCustomerRequest duplicateRequest = new RegisterCustomerRequest(
                "testuser2@test.com", "password456", "김철수", "010-1234-5678"
            );

            // When & Then
            mockMvc.perform(post("/api/v1/customer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400 에러가 발생한다.")
        void register_MissingRequiredFields_ReturnsBadRequest() throws Exception {
            // Given - loginId가 누락된 요청
            String invalidJson = """
                {
                    "password": "password123",
                    "name": "홍길동",
                    "phoneNumber": "010-1234-5678"
                }
                """;

            // When & Then
            mockMvc.perform(post("/api/v1/customer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("빈 요청 본문으로 요청하면 400 에러가 발생한다.")
        void register_EmptyRequestBody_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/customer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
} 