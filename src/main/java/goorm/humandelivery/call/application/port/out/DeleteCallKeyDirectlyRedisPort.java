package goorm.humandelivery.call.application.port.out;

public interface DeleteCallKeyDirectlyRedisPort {

    void deleteCallKey(Long callId);

}
