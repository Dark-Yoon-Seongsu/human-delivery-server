package goorm.humandelivery.driver.application.port.out;

public interface GetDriverLastUpdateRedisPort {

    String getLastUpdate(String driverLoginId);

}