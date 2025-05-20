package goorm.humandelivery.driver.application.port.out;

import java.util.Set;

public interface GetActiveDriversRedisPort {

    Set<String> getActiveDrivers();

}
