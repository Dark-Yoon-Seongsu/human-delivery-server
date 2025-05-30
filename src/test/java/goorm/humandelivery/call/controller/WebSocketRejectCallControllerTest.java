package goorm.humandelivery.call.controller;

import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
import goorm.humandelivery.call.application.port.out.SetCallWithPort;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.domain.CallStatus;
import goorm.humandelivery.call.dto.request.CallRejectRequest;
import goorm.humandelivery.call.dto.response.CallRejectResponse;
import goorm.humandelivery.call.infrastructure.persistence.JpaCallInfoRepository;
import goorm.humandelivery.customer.application.port.out.SaveCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.infrastructure.persistence.JpaCustomerRepository;
import goorm.humandelivery.driver.application.port.in.ChangeTaxiDriverStatusUseCase;
import goorm.humandelivery.driver.application.port.in.RegisterTaxiDriverUseCase;
import goorm.humandelivery.driver.application.port.in.UpdateDriverLocationUseCase;
import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.request.RegisterTaxiDriverRequest;
import goorm.humandelivery.driver.dto.request.RegisterTaxiRequest;
import goorm.humandelivery.driver.dto.request.UpdateDriverLocationRequest;
import goorm.humandelivery.driver.infrastructure.persistence.JpaTaxiDriverRepository;
import goorm.humandelivery.driver.infrastructure.persistence.JpaTaxiRepository;
import goorm.humandelivery.shared.dto.response.ErrorResponse;
import goorm.humandelivery.shared.location.domain.Location;
import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketRejectCallControllerTest {

    static final Logger log = LoggerFactory.getLogger(WebSocketRejectCallControllerTest.class);

    @LocalServerPort
    int port;

    @Autowired
    SetCallWithPort setCallWithPort;

    @Autowired
    SaveCustomerPort saveCustomerPort;

    @Autowired
    SaveCallInfoPort saveCallInfoPort;

    @Autowired
    RegisterTaxiDriverUseCase registerTaxiDriverUseCase;

    @Autowired
    ChangeTaxiDriverStatusUseCase changeTaxiDriverStatusUseCase;

    @Autowired
    UpdateDriverLocationUseCase updateDriverLocationUseCase;

    @Autowired
    JwtTokenProviderPort jwtTokenProviderPort;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    WebSocketStompClient webSocketStompClient;

    @Autowired
    JpaTaxiRepository jpaTaxiRepository;

    @Autowired
    JpaTaxiDriverRepository jpaTaxiDriverRepository;

    @Autowired
    JpaCustomerRepository jpaCustomerRepository;

    @Autowired
    JpaCallInfoRepository jpaCallInfoRepository;

    @AfterEach
    void tearDown() {
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
    @DisplayName("콜 ID를 통해 콜 요청을 거절할 수 있다.")
    void rejectTaxiCall() throws Exception {
        // Given
        // 택시 생성하기
        RegisterTaxiRequest registerTaxiRequest = new RegisterTaxiRequest();
        registerTaxiRequest.setModel("Sonata");
        registerTaxiRequest.setTaxiType("NORMAL");
        registerTaxiRequest.setFuelType("GASOLINE");
        registerTaxiRequest.setPlateNumber("12가1234");

        // 택시기사 생성하기
        RegisterTaxiDriverRequest request = new RegisterTaxiDriverRequest();
        request.setLoginId("driver1@email.com");
        request.setPassword("1234");
        request.setName("홍길동");
        request.setPhoneNumber("010-1234-5678");
        request.setLicenseCode("LIC123456");
        request.setTaxi(registerTaxiRequest);

        registerTaxiDriverUseCase.register(request);

        // 택시기사 상태변경
        changeTaxiDriverStatusUseCase.changeStatus("driver1@email.com", TaxiDriverStatus.AVAILABLE);

        UpdateDriverLocationRequest updateDriverLocationRequest = new UpdateDriverLocationRequest();
        updateDriverLocationRequest.setCustomerLoginId(null);
        updateDriverLocationRequest.setLocation(new Location(38.198592, 11.098888));
        updateDriverLocationUseCase.updateLocation(updateDriverLocationRequest, "driver1@email.com");

        // JWT 발급
        String token = jwtTokenProviderPort.generateToken("driver1@email.com");

        // 웹소켓 설정
        webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", token);

        WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
        String url = String.format("ws://localhost:%d/ws", port);

        // 웹 소켓 연결
        CompletableFuture<CallRejectResponse> future = new CompletableFuture<>();
        StompSession stompSession = webSocketStompClient
                .connectAsync(url, webSocketHttpHeaders, stompHeaders, new StompSessionHandlerAdapter() {
                })
                .get(3, TimeUnit.SECONDS);

        // 응답 구독
        stompSession.subscribe("/user/queue/reject-call-result", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                log.info("getPayloadType 호출됨");
                return CallRejectResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("handleFrame 호출됨: payload = {}", payload);
                future.complete((CallRejectResponse) payload);
            }
        });

        // 콜 요청하기
        Customer savedCustomer = saveCustomerPort.save(new Customer("test", "test", "test", "test"));
        Location expectedOrigin = new Location(38.2, 11.1);
        Location expectedDestination = new Location(39.3, 12.2);
        CallInfo callInfo = new CallInfo(null, savedCustomer, expectedOrigin, expectedDestination, TaxiType.NORMAL);
        CallInfo savedCallInfo = saveCallInfoPort.save(callInfo);
        setCallWithPort.setCallWith(savedCallInfo.getId(), CallStatus.SENT);

        Long target = savedCallInfo.getId();
        CallRejectRequest callRejectRequest = new CallRejectRequest();
        callRejectRequest.setCallId(target);

        // When
        stompSession.send("/app/taxi-driver/reject-call", callRejectRequest);

        // Then
        CallRejectResponse callRejectResponse = future.get(10, TimeUnit.SECONDS);
        assertThat(callRejectResponse).isNotNull();
        assertThat(callRejectResponse.getCallId()).isEqualTo(target);
    }

    @Test
    @DisplayName("콜 요청 거절시 콜 ID가 존재하지 않으면 예외가 발생한다.")
    void rejectTaxiCallWithNoCallId() throws Exception {
        // Given
        String token = jwtTokenProviderPort.generateToken("driver1@email.com");
        webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", token);

        WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
        String url = String.format("ws://localhost:%d/ws", port);

        // 웹 소켓 연결
        CompletableFuture<ErrorResponse> future = new CompletableFuture<>();
        StompSession stompSession = webSocketStompClient
                .connectAsync(url, webSocketHttpHeaders, stompHeaders, new StompSessionHandlerAdapter() {
                })
                .get(3, TimeUnit.SECONDS);

        stompSession.subscribe("/user/queue/errors", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ErrorResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                future.complete((ErrorResponse) payload);
            }
        });

        CallRejectRequest callRejectRequest = new CallRejectRequest();

        // When
        stompSession.send("/app/taxi-driver/reject-call", callRejectRequest);

        // Then
        ErrorResponse errorResponse = future.get(3, TimeUnit.SECONDS);
        assertThat(errorResponse.getCode()).isEqualTo("ERROR");
        assertThat(errorResponse.getMessage()).contains("콜 ID는 필수입니다.");
    }
}