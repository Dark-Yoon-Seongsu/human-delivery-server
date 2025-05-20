package goorm.humandelivery.driver.application.port.out;

import java.util.Optional;

public interface GetAssignedCallRedisPort {

    Optional<String> getCallIdByDriverId(String driverLoginId);

}
