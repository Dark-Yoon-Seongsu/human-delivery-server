package goorm.humandelivery.driver.controller.E2ETest;

import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.humandelivery.driver.dto.request.UpdateDriverLocationRequest;
import goorm.humandelivery.global.config.StompConfig;
import goorm.humandelivery.shared.dto.response.TokenInfoResponse;
import goorm.humandelivery.shared.location.domain.Location;
import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// ./gradlew test --tests "*WebSocketUpdateDriverLocationControllerE2ETest"
@Import({StompConfig.class, WebSocketUpdateDriverLocationControllerE2ETest.FailingJwtTokenProviderConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebSocketUpdateDriverLocationControllerE2ETest {

    @TestConfiguration
    static class FailingJwtTokenProviderConfig {

        @Bean
        @Primary
        public JwtTokenProviderPort jwtTokenProviderPort() {
            return new JwtTokenProviderPort() {
                @Override
                public String generateToken(String loginId){
                // 테스트에서는 항상 유효한 토큰으로 간주
                    return "Bearer dummy" + loginId + "-test-token";
                }

                @Override
                public boolean validateToken(String token){
                    // 테스트에서는 항상 유효한토큰으로 간주
                    return true;
                }

                @Override
                public TokenInfoResponse extractTokenInfo(String token) {
                    // 테스트용 인증 객체 반환
                    return null;
                }

                @Override
                public Authentication getAuthentication(String token) {
                    return new UsernamePasswordAuthenticationToken("driver1@example.com", null, Collections.emptyList());
                }
            };
        }
    }

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    private final static String WS_URI = "ws://localhost:%d/ws";
    private final static String TEST_JWT = "Bearer dummy-test-token";


    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @DisplayName("1. 위치 갱신 성공")
    void updateLocationSuccess() throws Exception {
        String customerLoginId = "customer1@example.com";

        // 헤더에 JWT 토큰 추가
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", TEST_JWT);

        CompletableFuture<StompSession> futureSession = stompClient.connectAsync(
                String.format(WS_URI, port),
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {}
        );

        StompSession session = futureSession.get(3, TimeUnit.SECONDS);

        // 위치 갱신 요청 객체
        UpdateDriverLocationRequest request = new UpdateDriverLocationRequest(
                customerLoginId,
                new Location(37.5665, 126.9780)
        );

        session.send("/app/taxi-driver/update-location", request);

        assertThat(session.isConnected()).isTrue();
    }
}

