package goorm.humandelivery.driver.controller.E2ETest;

import goorm.humandelivery.driver.application.port.out.SaveTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//./gradlew test --tests "*LoginTaxiDriverControllerMockMvcE2ETest"
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class LoginTaxiDriverControllerMockMvcE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SaveTaxiDriverPort saveTaxiDriverPort;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final String validLoginId = "driver1@example.com";
    private final String rawPassword = "password123";

    @BeforeEach
    void setUp() {
        TaxiDriver driver = TaxiDriver.builder()
                .loginId(validLoginId)
                .password(passwordEncoder.encode(rawPassword))
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();
        saveTaxiDriverPort.save(driver);
    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() throws Exception {
        String jsonRequest = """
            {
                "loginId": "%s",
                "password": "%s"
            }
            """.formatted(validLoginId, rawPassword);

        mockMvc.perform(post("/api/v1/taxi-driver/auth-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 ID 로그인 실패")
    void loginFailNotFound() throws Exception {
        String jsonRequest = """
            {
                "loginId": "nonexistent@example.com",
                "password": "%s"
            }
            """.formatted(rawPassword);

        mockMvc.perform(post("/api/v1/taxi-driver/auth-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("아이디에 해당하는 엔티티가 존재하지 않습니다.")));
    }

    @Test
    @DisplayName("비밀번호 불일치 로그인 실패")
    void loginFailWrongPassword() throws Exception {
        String jsonRequest = """
            {
                "loginId": "%s",
                "password": "wrongpassword"
            }
            """.formatted(validLoginId);

        mockMvc.perform(post("/api/v1/taxi-driver/auth-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("패스워드가 일치하지 않습니다")));
    }
}
