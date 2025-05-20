package goorm.humandelivery.call.application.port.in;

public interface DeleteMatchingUseCase {

    void deleteByCallId(Long callId);

}
