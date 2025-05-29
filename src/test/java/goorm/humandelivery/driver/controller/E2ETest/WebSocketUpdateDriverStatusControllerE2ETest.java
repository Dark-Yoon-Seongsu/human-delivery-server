package goorm.humandelivery.driver.controller.E2ETest;

import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.humandelivery.driver.application.port.in.UpdateDriverStatusUseCase;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.dto.request.UpdateTaxiDriverStatusRequest;
import goorm.humandelivery.driver.dto.response.UpdateTaxiDriverStatusResponse;
import goorm.humandelivery.global.config.StompConfig;
import goorm.humandelivery.shared.dto.response.TokenInfoResponse;
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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
// ./gradlew test --tests "*WebSocketUpdateDriverStatusControllerE2ETest"
@Import({StompConfig.class, WebSocketUpdateDriverStatusControllerE2ETest.FakeJwtTokenProviderConfig.class, WebSocketUpdateDriverStatusControllerE2ETest.MockServiceConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WebSocketUpdateDriverStatusControllerE2ETest {

    @TestConfiguration
    static class MockServiceConfig {

        @Bean
        @Primary
        public UpdateDriverStatusUseCase updateDriverStatusUseCase() {
            UpdateDriverStatusUseCase mock = Mockito.mock(UpdateDriverStatusUseCase.class);
            Mockito.when(mock.updateStatus(Mockito.any(), Mockito.any()))
                    .thenReturn(new UpdateTaxiDriverStatusResponse(TaxiDriverStatus.AVAILABLE));
            return mock;
        }
    }


    @TestConfiguration
    static class FakeJwtTokenProviderConfig {
        @Bean
        @Primary
        public JwtTokenProviderPort jwtTokenProviderPort() {
            return new JwtTokenProviderPort() {
                @Override
                public String generateToken(String loginId) {
                    return "Bearer dummy" + loginId + "-test-token";
                }

                @Override
                public boolean validateToken(String token) {
                    return true;
                }

                @Override
                public TokenInfoResponse extractTokenInfo(String token) {
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
    private final static String TEST_JWT = "Bearer dummydriver1@example.com-test-token";

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @DisplayName("1. 기사 상태 업데이트 성공")
    void updateStatusSuccess() throws Exception {
        // WebSocket 연결 헤더 설정
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", TEST_JWT);

        // WebSocket 세션 연결
        CompletableFuture<StompSession> futureSession = stompClient.connectAsync(
                String.format(WS_URI, port),
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {}
        );
        StompSession session = futureSession.get(5, TimeUnit.SECONDS);

        // 응답 메시지를 비동기로 받을 준비
        CompletableFuture<UpdateTaxiDriverStatusResponse> futureResponse = new CompletableFuture<>();

        session.subscribe("/user/queue/taxi-driver-status", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return UpdateTaxiDriverStatusResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                futureResponse.complete((UpdateTaxiDriverStatusResponse) payload);
            }
        });

        Thread.sleep(1000);

        // 요청 생성 및 전송
        UpdateTaxiDriverStatusRequest request = new UpdateTaxiDriverStatusRequest("빈차");
        session.send("/app/taxi-driver/update-status", request);

        // 응답 수신 및 검증


        UpdateTaxiDriverStatusResponse response = futureResponse.get(3, TimeUnit.SECONDS);
        assertThat(response).isNotNull();
        assertThat(response.getTaxiDriverStatus()).isEqualTo(TaxiDriverStatus.AVAILABLE);
    }
}

