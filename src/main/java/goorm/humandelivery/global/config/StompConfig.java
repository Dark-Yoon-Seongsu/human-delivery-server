package goorm.humandelivery.global.config;

import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProviderPort jwtTokenProviderPort;

    @Autowired
    public StompConfig(JwtTokenProviderPort jwtTokenProviderPort) {
        this.jwtTokenProviderPort = jwtTokenProviderPort;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 연결할 WebSocket 핸드쉐이크용 HTTP URL
        // 인증, 콜 요청
        registry.addEndpoint("/ws").setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // setApplicationDestinationPrefixes 메세지는 컨트롤러의 @MessageMapping 메서드로 라우팅

        /**
         * 	simpleBroker(내장브로커) 사용 -> 추후 외부 브로커 시스템으로 변경(튜닝 포인트)
         * 	/topic : 관례상 pup/sub 구조에서 사용
         * 	/queue : 관례상 일대일 메세지 전송에서 사용.
         */

        config.enableSimpleBroker("/topic", "/queue");
        config.setUserDestinationPrefix("/user");

        /**
         * RabbitMQ 를 외부 메세지 브로커로 사용.
         */
//        config.enableStompBrokerRelay("/queue", "/topic")
//                .setRelayHost("localhost")
//                .setRelayPort(61613)
//                .setClientLogin("guest")
//                .setClientPasscode("guest")
//                .setSystemLogin("guest")
//                .setSystemPasscode("guest")
//                .setSystemHeartbeatSendInterval(10000)
//                .setSystemHeartbeatReceiveInterval(10000);

        config.setApplicationDestinationPrefixes("/app");

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 인바운드 채널
        // 클라이언트 -> 웹소켓 서버로 보내는 통로.
        registration.interceptors(new ChannelInterceptor() {

            // ChannelInterceptor 이놈이 그 메세지를 가로채서, 무언가 할 수 있게 해준다.
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                StompCommand command = accessor != null ? accessor.getCommand() : null;

                if (accessor == null) {
                    return message;
                }

                String sessionId = accessor.getSessionId();
                Principal principal = accessor.getUser();
                String username = (principal != null) ? principal.getName() : "익명";
                String destination = accessor.getDestination();

                log.info("[WebSocket] cmd={}, sessionId={}, user={}, dest={} payload={}", command, sessionId, username, destination, accessor.getMessage());

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    try {
                        String authHeader = accessor.getFirstNativeHeader("Authorization");
                        log.info("Received Authorization header: {}", authHeader);
                        if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
                            String token = authHeader.substring(7); // "Bearer " 다음의 토큰 부분만 추출
                            log.info("Extracted token: {}", token);
                            boolean isValid = jwtTokenProviderPort.validateToken(token);

                            if (!isValid) {
                                log.warn("Invalid JWT token received: {}", token);
                                throw new IllegalArgumentException("Invalid JWT Token");
                            }

                            Authentication authentication = jwtTokenProviderPort.getAuthentication(token);
                            log.info("Successfully authenticated via token for user: {}", authentication.getName());
                            accessor.setUser(authentication);
                        } else {
                            log.warn("Authorization header is missing or does not start with Bearer. Header: {}", authHeader);
                            throw new IllegalArgumentException("Authorization header is missing or invalid");
                        }
                    } catch (Exception e) {
                        log.warn("WebSocket 인증 중 예외 발생: {}", e.getMessage(), e);
                        throw new IllegalArgumentException("Invalid WebSocket Token", e);
                    }
                }
                return message;
            }
        });
    }

}
