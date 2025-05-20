package goorm.humandelivery.driver.application.port.out;

public interface DeleteAssignedCallRedisPort {

    void deleteAssignedCallOf(String driverLoginId);

}
