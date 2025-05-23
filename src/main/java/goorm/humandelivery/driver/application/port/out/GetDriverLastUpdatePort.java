package goorm.humandelivery.driver.application.port.out;

public interface GetDriverLastUpdatePort {

    String getLastUpdate(String driverLoginId);

}