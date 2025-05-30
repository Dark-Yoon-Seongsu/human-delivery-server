package goorm.humandelivery.driving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.humandelivery.call.dto.request.CallIdRequest;
import goorm.humandelivery.driving.application.port.in.RideFinishUseCase;
import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.WebSocketHttpHeaders;

import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class WebSocketRideFinishIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProviderPort jwtTokenProviderPort;

    @SpyBean
    private RideFinishUseCase rideFinishUseCase;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() throws Exception {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String token = jwtTokenProviderPort.generateToken("testDriver");
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        stompSession = stompClient.connectAsync(
                "ws://localhost:" + port + "/ws",
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {}
        ).get(1, SECONDS);
    }

    @Test
    @DisplayName("웹소켓을 통해 ride-finish 메시지를 전송하고 응답을 받는다")
    void shouldSendRideFinishMessage() throws Exception {
        // given
        Long callId = 1L;
        CallIdRequest request = new CallIdRequest();
        request.setCallId(callId);

        // when
        stompSession.send("/app/taxi-driver/ride-finish", request);

        // then
        verify(rideFinishUseCase, timeout(1000).times(1)).finish(eq(callId), eq("testDriver"));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 ride-finish 메시지를 전송하면 에러가 발생한다")
    void shouldThrowErrorWhenUnauthorized() throws Exception {
        // given
        WebSocketStompClient unauthorizedClient = new WebSocketStompClient(new StandardWebSocketClient());
        unauthorizedClient.setMessageConverter(new MappingJackson2MessageConverter());

        // when & then
        assertThatThrownBy(() -> {
            unauthorizedClient.connectAsync(
                    "ws://localhost:" + port + "/ws",
                    new WebSocketHttpHeaders(),
                    new StompSessionHandlerAdapter() {}
            ).get(1, SECONDS);
        }).isInstanceOf(ExecutionException.class);
    }
} 