package goorm.humandelivery.driver.controller.E2ETest;

import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.humandelivery.driver.domain.FuelType;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.request.RegisterTaxiDriverRequest;
import goorm.humandelivery.driver.dto.request.RegisterTaxiRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//./gradlew test --tests "*RegisterTaxiDriverControllerE2ETest"

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class RegisterTaxiDriverControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. 정상적인 회원가입 요청은 200 OK와 사용자 정보 반환")
    void registerSuccess() throws Exception {
        RegisterTaxiRequest taxiRequest = RegisterTaxiRequest.builder()
                .model("Sonata")
                .plateNumber("12가3456")
                .taxiType(String.valueOf(TaxiType.NORMAL))
                .fuelType(String.valueOf(FuelType.GASOLINE))
                .build();

        RegisterTaxiDriverRequest request = RegisterTaxiDriverRequest.builder()
                .loginId("newdriver@example.com")
                .password("securePassword123!")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .licenseCode("LIC123456")
                .taxi(taxiRequest)
                .build();

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/taxi-driver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("newdriver@example.com"));
    }

    @Test
    @DisplayName("2. 이미 존재하는 loginId로 회원가입하면 400 예외 반환")
    void registerFailDuplicateLoginId() throws Exception {
        // 먼저 한번 등록
        registerSuccess();

        RegisterTaxiRequest taxiRequest = RegisterTaxiRequest.builder()
                .model("K5")
                .plateNumber("34나5678")
                .taxiType(String.valueOf(TaxiType.NORMAL))
                .fuelType(String.valueOf(FuelType.GASOLINE))
                .build();

        RegisterTaxiDriverRequest request = RegisterTaxiDriverRequest.builder()
                .loginId("newdriver@example.com") // 같은 ID
                .password("anotherPassword")
                .name("김철수")
                .phoneNumber("010-9876-5432")
                .licenseCode("LIC999999")
                .taxi(taxiRequest)
                .build();

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/taxi-driver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("이미 사용 중인 아이디입니다.")));
    }

    @Test
    @DisplayName("3. 잘못된 형식의 이메일로 요청 시 400 예외 반환")
    void registerFailInvalidEmail() throws Exception {
        RegisterTaxiRequest taxiRequest = RegisterTaxiRequest.builder()
                .model("Avante")
                .plateNumber("78다9012")
                .taxiType(String.valueOf(TaxiType.NORMAL))
                .fuelType(String.valueOf(FuelType.GASOLINE))
                .build();

        RegisterTaxiDriverRequest request = RegisterTaxiDriverRequest.builder()
                .loginId("invalid-email") // 잘못된 형식
                .password("password")
                .name("이영희")
                .phoneNumber("010-3333-4444")
                .licenseCode("LIC222222")
                .taxi(taxiRequest)
                .build();

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/taxi-driver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("must be a well-formed email addres"))));
    }
}
