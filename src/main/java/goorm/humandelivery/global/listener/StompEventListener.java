package goorm.humandelivery.global.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

@Component
@Slf4j
public class StompEventListener {

    @EventListener
    public void listener(SessionConnectEvent sessionConnectEvent) {
        log.info("sessionConnectEvent. {}", sessionConnectEvent);
    }

    @EventListener
    public void listener(SessionConnectedEvent sessionConnectedEvent) {
        log.info("sessionConnectedEvent. {}", sessionConnectedEvent);
    }

    @EventListener
    public void listener(SessionSubscribeEvent sessionSubscribeEvent) {
        log.info("sessionSubscribeEvent. {}", sessionSubscribeEvent);
    }

    @EventListener
    public void listener(SessionUnsubscribeEvent sessionUnsubscribeEvent) {
        log.info("sessionUnsubscribeEvent. {}", sessionUnsubscribeEvent);
    }

    @EventListener
    public void listener(SessionDisconnectEvent sessionDisconnectEvent) {
        log.info("sessionDisconnectEvent. {}", sessionDisconnectEvent);
    }
}
