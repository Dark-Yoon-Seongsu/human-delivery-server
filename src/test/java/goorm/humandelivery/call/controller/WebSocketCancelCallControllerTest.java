package goorm.humandelivery.call.controller;

import goorm.humandelivery.call.application.port.out.LoadCallInfoPort;
import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
import goorm.humandelivery.call.application.port.out.SetCallWithPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.domain.CallStatus;
import goorm.humandelivery.call.dto.request.CancelCallMessage;
import goorm.humandelivery.call.infrastructure.persistence.JpaCallInfoRepository;
import goorm.humandelivery.call.infrastructure.persistence.JpaMatchingRepository;
import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.dto.response.CallCancelMessageResponse;
import goorm.humandelivery.customer.infrastructure.persistence.JpaCustomerRepository;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.infrastructure.persistence.JpaTaxiDriverRepository;
import goorm.humandelivery.driver.infrastructure.persistence.JpaTaxiRepository;
import goorm.humandelivery.global.config.StompConfig;
import goorm.humandelivery.global.exception.CallInfoEntityNotFoundException;
import goorm.humandelivery.shared.location.domain.Location;
import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
class WebSocketCancelCallControllerTest {
    private static final Logger log = LoggerFactory.getLogger(WebSocketCancelCallControllerTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    JpaCallInfoRepository jpaCallInfoRepository;

    @Autowired
    JpaCustomerRepository jpaCustomerRepository;

    @Autowired
    JpaTaxiDriverRepository jpaTaxiDriverRepository;

    @Autowired
    JpaMatchingRepository jpaMatchingRepository;

    @Autowired
    JpaTaxiRepository jpaTaxiRepository;

    @Autowired
    SetCallWithPort setCallWithPort;

    @Autowired
    JwtTokenProviderPort jwtTokenProviderPort;

    @Autowired
    private SaveCallInfoPort saveCallInfoPort;

    @Autowired
    private LoadCallInfoPort loadCallInfoPort;

    @Autowired
    private SaveCustomerPort saveCustomerPort;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    WebSocketStompClient webSocketStompClient;

    @AfterEach
    void tearDown() {
        jpaMatchingRepository.deleteAllInBatch();
        jpaCallInfoRepository.deleteAllInBatch();
        jpaCustomerRepository.deleteAllInBatch();
        jpaTaxiDriverRepository.deleteAllInBatch();
        jpaTaxiRepository.deleteAllInBatch();
        stringRedisTemplate.getConnectionFactory().getConnection().commands().flushAll();

        if (webSocketStompClient != null) {
            webSocketStompClient.stop();
        }
    }

    @Test
    @DisplayName("WebSocket으로 콜 취소 요청시 성공적으로 콜이 취소된다")
    void cancelCallViaWebSocket_success() throws Exception {
        // GIVEN: 고객 및 콜 정보 저장
        Customer savedCustomer = saveCustomerPort.save(Customer.builder()
                .loginId("customer1@example.com")
                .password("fas@df.com")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .build());

        Location origin = new Location(37.5665, 126.9780);
        Location destination = new Location(37.5651, 126.9890);
        TaxiType taxiType = TaxiType.NORMAL;
        CallInfo callInfo = new CallInfo(null, savedCustomer, origin, destination, taxiType);
        Long callId = saveCallInfoPort.save(callInfo).getId();
        setCallWithPort.setCallWith(callId, CallStatus.SENT);

        CountDownLatch latch = new CountDownLatch(1);

        // JWT 발급
        String token = jwtTokenProviderPort.generateToken("customer1@example.com");

        // 웹소켓 설정
        webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", "Bearer " + token);

        WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
        String url = String.format("ws://localhost:%d/ws", port);

        // 웹 소켓 연결
        CompletableFuture<CallCancelMessageResponse> future = new CompletableFuture<>();
        StompSession stompSession = webSocketStompClient
                .connectAsync(url, webSocketHttpHeaders, stompHeaders, new StompSessionHandlerAdapter() {
                })
                .get(10, TimeUnit.SECONDS);
        String sessionId = stompSession.getSessionId();
        String userQueue = "/user/queue/call-cancelled";

        stompSession.subscribe(userQueue, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                log.info("getPayloadType 호출됨");
                return CallCancelMessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("handleFrame 호출됨: payload = {}", payload);
                future.complete((CallCancelMessageResponse) payload);
                latch.countDown();
            }
        });

        // subscribe 완료를 확실히 기다리기 위한 임시 방법 (필요하면 개선)
        Thread.sleep(1000);

        CancelCallMessage cancelMessage = new CancelCallMessage(callId);

        stompSession.send("/app/call/cancel", cancelMessage);

        // 응답 대기 (기본 10초)
        boolean received = latch.await(10, TimeUnit.SECONDS);
        assertTrue(received, "콜 취소 응답을 수신하지 못했습니다.");

        // THEN: 응답 검증 및 DB 상태 확인
        CallCancelMessageResponse response = future.get(10, TimeUnit.SECONDS);

        assertThat(response).isNotNull();
        assertThat(response.getCallId()).isEqualTo(callId);
        assertThat(response.getMessage()).isEqualTo("콜이 성공적으로 취소되었습니다.");

        CallInfo updatedCall = loadCallInfoPort.findCallInfoAndCustomerByCallId(callId)
                .orElseThrow(CallInfoEntityNotFoundException::new);
        assertThat(updatedCall.getStatus()).isEqualTo(CallStatus.CANCELLED);
    }

}
