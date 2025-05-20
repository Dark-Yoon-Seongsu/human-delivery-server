package goorm.humandelivery.driver.application.port.out;

public interface DeleteActiveDriverRedisPort {

    void setOffDuty(String taxiDriverLoginId);

}
