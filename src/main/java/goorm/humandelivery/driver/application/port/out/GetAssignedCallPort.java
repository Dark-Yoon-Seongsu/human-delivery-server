package goorm.humandelivery.driver.application.port.out;

import java.util.Optional;

public interface GetAssignedCallPort {

    Optional<String> getCallIdByDriverId(String driverLoginId);

}
