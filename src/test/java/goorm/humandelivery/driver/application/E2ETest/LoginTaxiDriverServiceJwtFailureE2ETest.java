package goorm.humandelivery.driver.application.E2ETest;

import goorm.humandelivery.driver.application.LoginTaxiDriverService;
import goorm.humandelivery.driver.dto.request.LoginTaxiDriverRequest;
import goorm.humandelivery.global.exception.JwtTokenGenerationException;
import goorm.humandelivery.shared.dto.response.TokenInfoResponse;
import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(LoginTaxiDriverServiceJwtFailureE2ETest.FailingJwtTokenProviderConfig.class)
class LoginTaxiDriverServiceJwtFailureE2ETest {

    @Autowired
    private LoginTaxiDriverService loginTaxiDriverService;

    private final String validLoginId = "testdriver";
    private final String rawPassword = "password123";

    @Test
    @DisplayName("4. JWT 토큰 생성 실패 시 예외 발생")
    void loginFailJwtGenerationException() {
        LoginTaxiDriverRequest request = LoginTaxiDriverRequest.builder()
                .loginId(validLoginId)
                .password(rawPassword)
                .build();

        assertThatThrownBy(() -> loginTaxiDriverService.login(request))
                .isInstanceOf(JwtTokenGenerationException.class)
                .hasMessageContaining("JWT 토큰 발급에 실패했습니다");
    }

    @TestConfiguration
    static class FailingJwtTokenProviderConfig {
        @Bean
        public JwtTokenProviderPort jwtTokenProviderPort() {
            return new JwtTokenProviderPort() {
                @Override
                public String generateToken(String loginId) {
                    throw new JwtTokenGenerationException("JWT 토큰 발급에 실패했습니다.", new RuntimeException());
                }

                @Override
                public boolean validateToken(String token) {
                    return false;
                }

                @Override
                public TokenInfoResponse extractTokenInfo(String token) {
                    return null;
                }

                @Override
                public Authentication getAuthentication(String token) {
                    return null;
                }
            };
        }
    }
}
