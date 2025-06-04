//package goorm.humandelivery.customer.controller;
//
//import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
//import goorm.humandelivery.call.domain.CallInfo;
//import goorm.humandelivery.call.domain.CallStatus;
//import goorm.humandelivery.call.dto.request.CancelCallMessage;
//import goorm.humandelivery.call.infrastructure.persistence.JpaCallInfoRepository;
//import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
//import goorm.humandelivery.customer.domain.Customer;
//import goorm.humandelivery.customer.dto.response.CallCancelMessageResponse;
//import goorm.humandelivery.customer.infrastructure.persistence.JpaCustomerRepository;
//import goorm.humandelivery.driver.domain.TaxiType;
//import goorm.humandelivery.global.config.StompConfig;
//import goorm.humandelivery.shared.dto.response.TokenInfoResponse;
//import goorm.humandelivery.shared.location.domain.Location;
//import goorm.humandelivery.shared.messaging.CallMessage;
//import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.context.annotation.Primary;
//import org.springframework.messaging.converter.MappingJackson2MessageConverter;
//import org.springframework.messaging.simp.stomp.*;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.web.socket.WebSocketHttpHeaders;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.messaging.WebSocketStompClient;
//
//import java.lang.reflect.Type;
//import java.util.Collections;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@Import({StompConfig.class, WebSocketCancelCallControllerTest.TestJwtConfig.class})
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//class WebSocketCancelCallControllerTest {
//
//    @TestConfiguration
//    static class TestJwtConfig {
//        @Bean
//        @Primary
//        public JwtTokenProviderPort jwtTokenProviderPort() {
//            return new JwtTokenProviderPort() {
//                @Override
//                public String generateToken(String loginId) {
//                    return "Bearer dummy-" + loginId;
//                }
//
//                @Override
//                public boolean validateToken(String token) {
//                    return true;
//                }
//
//                @Override
//                public TokenInfoResponse extractTokenInfo(String token) {
//                    // 테스트용 인증 객체 반환
//                    return null;
//                }
//
//                @Override
//                public Authentication getAuthentication(String token) {
//                    return new UsernamePasswordAuthenticationToken("driver1@example.com", null, Collections.emptyList());
//                }
//            };
//        }
//    }
//
//    @LocalServerPort
//    private int port;
//
//    private WebSocketStompClient stompClient;
//
//    private final static String WS_URI = "ws://localhost:%d/ws";
//    private final static String TEST_JWT = "Bearer dummy-customer1@example.com";
//
//    @BeforeEach
//    void setup() {
//        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
//        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//    }
//
//    @Autowired
//    private SaveCallInfoPort saveCallInfoPort;
//
//    @Autowired
//    private SaveCustomerPort saveCustomerPort;
//
//    @Test
//    @DisplayName("WebSocket으로 콜 취소 요청시 성공적으로 콜이 취소된다")
//    void cancelCallViaWebSocket_success() throws Exception {
//        // GIVEN: 테스트용 콜 정보 저장
//        Customer customer = Customer.builder()
//                .loginId("fas@df.com").password("fas@df.com").name("테스트 고객").phoneNumber("010-1234-5678").build();
//        Customer savedCustomer = saveCustomerPort.save(customer);
//
//        Location expectedOrigin = new Location(37.5665, 126.978);
//        Location expectedDestination = new Location(37.5651, 126.989);
//        TaxiType taxiType = TaxiType.NORMAL;
//        CallInfo callInfo = new CallInfo(
//                null,
//                savedCustomer,
//                expectedOrigin,
//                expectedDestination,
//                taxiType
//        );
//        CallInfo savedCall = saveCallInfoPort.save(callInfo);
//
//        Long callId = savedCall.getId();
//
//        // WebSocket 연결
//        StompHeaders connectHeaders = new StompHeaders();
//        connectHeaders.add("Authorization", TEST_JWT);
//
//        StompSession session = stompClient.connectAsync(
//                String.format(WS_URI, port),
//                new WebSocketHttpHeaders(),
//                connectHeaders,
//                new StompSessionHandlerAdapter() {}
//        ).get(3, TimeUnit.SECONDS);
//
//        assertThat(session.isConnected()).isTrue();
//
//        // 응답 받을 CompletableFuture
//        CompletableFuture<CallCancelMessageResponse> future = new CompletableFuture<>();
//
//        session.subscribe("/user/queue/call/cancelled", new StompFrameHandler() {
//            @Override
//            public Type getPayloadType(StompHeaders headers) {
//                return CallCancelMessageResponse.class;
//            }
//
//            @Override
//            public void handleFrame(StompHeaders headers, Object payload) {
//                future.complete((CallCancelMessageResponse) payload);
//            }
//        });
//
//        // WHEN: 콜 취소 요청 전송
//        CallMessage message = new CallMessage(callId, savedCustomer.getLoginId(), expectedOrigin, expectedDestination, taxiType, 0);
//
//        session.send("/app/call/cancel", message);
//
//        // THEN: 콜 취소 응답 수신 및 검증
//        CallCancelMessageResponse response = future.get(5, TimeUnit.SECONDS);
//        assertThat(response).isNotNull();
//        assertThat(response.getCallId()).isEqualTo(callId);
//        assertThat(response.getMessage()).isEqualTo("콜이 성공적으로 취소되었습니다.");
//    }
//}
