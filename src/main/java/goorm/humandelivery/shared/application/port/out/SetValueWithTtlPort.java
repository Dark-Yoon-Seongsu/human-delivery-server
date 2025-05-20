package goorm.humandelivery.shared.application.port.out;

import java.time.Duration;

public interface SetValueWithTtlPort {

    void setValueWithTTL(String key, String value, Duration ttl);

}