package goorm.humandelivery.call.application.port.out;

public interface DeleteCallStatusRedisPort {

    void deleteCallStatus(Long callId);

}