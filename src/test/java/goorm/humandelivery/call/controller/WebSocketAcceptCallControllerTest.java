package goorm.humandelivery.call.controller;

import goorm.humandelivery.call.application.port.in.RequestCallUseCase;
import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.call.dto.request.CallAcceptRequest;
import goorm.humandelivery.call.dto.request.CallMessageRequest;
import goorm.humandelivery.call.dto.response.CallAcceptResponse;
import goorm.humandelivery.call.infrastructure.persistence.JpaCallInfoRepository;
import goorm.humandelivery.call.infrastructure.persistence.JpaMatchingRepository;
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
import goorm.humandelivery.shared.location.domain.Location;
import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
class WebSocketAcceptCallControllerTest {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAcceptCallControllerTest.class);
    @LocalServerPort
    int port;

    @Autowired
    JwtTokenProviderPort jwtTokenProviderPort;

    @Autowired
    SaveCustomerPort saveCustomerPort;

    @Autowired
    RequestCallUseCase requestCallUseCase;

    @Autowired
    UpdateDriverLocationUseCase updateDriverLocationUseCase;

    @Autowired
    ChangeTaxiDriverStatusUseCase changeTaxiDriverStatusUseCase;

    @Autowired
    RegisterTaxiDriverUseCase registerTaxiDriverUseCase;

    @Autowired
    JpaTaxiRepository jpaTaxiRepository;

    @Autowired
    JpaTaxiDriverRepository jpaTaxiDriverRepository;

    @Autowired
    JpaCustomerRepository jpaCustomerRepository;

    @Autowired
    JpaCallInfoRepository jpaCallInfoRepository;

    @Autowired
    JpaMatchingRepository jpaMatchingRepository;

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
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        if (webSocketStompClient != null) {
            webSocketStompClient.stop();
        }
    }

    @Test
    @DisplayName("콜 ID를 통해 콜 요청을 수락할 수 있다.")
    void acceptTaxiCall() throws Exception {
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
        CompletableFuture<CallAcceptResponse> future = new CompletableFuture<>();
        StompSession stompSession = webSocketStompClient
                .connectAsync(url, webSocketHttpHeaders, stompHeaders, new StompSessionHandlerAdapter() {
                })
                .get(3, TimeUnit.SECONDS);

        // 응답 구독
        stompSession.subscribe("/user/queue/accept-call-result", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                log.info("getPayloadType 호출됨");
                return CallAcceptResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("handleFrame 호출됨: payload = {}", payload);
                future.complete((CallAcceptResponse) payload);
            }
        });

        // 콜 요청하기
        Customer savedCustomer = saveCustomerPort.save(new Customer("test", "test", "test", "test"));
        Location expectedOrigin = new Location(38.2, 11.1);
        Location expectedDestination = new Location(39.3, 12.2);
        requestCallUseCase.requestCall(new CallMessageRequest(expectedOrigin, expectedDestination, TaxiType.NORMAL, 1), savedCustomer.getLoginId());

        List<CallInfo> callInfos = jpaCallInfoRepository.findAll();
        CallInfo callInfo = callInfos.getFirst();
        Long target = callInfo.getId();
        CallAcceptRequest callAcceptRequest = new CallAcceptRequest();
        callAcceptRequest.setCallId(target);

        // When
        stompSession.send("/app/taxi-driver/accept-call", callAcceptRequest);

        // Then
        CallAcceptResponse callAcceptResponse = future.get(10, TimeUnit.SECONDS);
        assertThat(callAcceptResponse).isNotNull();
        assertThat(callAcceptResponse.getCallId()).isEqualTo(target);
        assertThat(callAcceptResponse.getExpectedOrigin()).isEqualTo(expectedOrigin);
        assertThat(callAcceptResponse.getExpectedDestination()).isEqualTo(expectedDestination);
    }
}