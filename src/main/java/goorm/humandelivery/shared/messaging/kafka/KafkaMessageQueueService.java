package goorm.humandelivery.shared.messaging.kafka;

import goorm.humandelivery.call.application.port.in.DeleteCallInfoUseCase;
import goorm.humandelivery.call.application.port.out.NotifyDispatchFailedToCustomerPort;
import goorm.humandelivery.call.application.port.out.SendCallRequestToDriverPort;
import goorm.humandelivery.call.application.port.out.SetCallWithPort;
import goorm.humandelivery.call.domain.CallStatus;
import goorm.humandelivery.shared.application.port.out.MessageQueuePort;
import goorm.humandelivery.shared.dto.response.ErrorResponse;
import goorm.humandelivery.shared.location.application.port.out.FindNearbyAvailableDriversPort;
import goorm.humandelivery.shared.messaging.CallMessage;
import goorm.humandelivery.shared.messaging.QueueMessage;
import goorm.humandelivery.global.exception.NoAvailableTaxiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageQueueService implements MessageQueuePort {

    private final KafkaMessageProducer kafkaMessageProducer;
    private final FindNearbyAvailableDriversPort findNearbyAvailableDriversPort;
    private final NotifyDispatchFailedToCustomerPort notifyDispatchFailedToCustomerPort;
    private final SetCallWithPort setCallWithPort;
    private final SendCallRequestToDriverPort sendCallRequestToDriverPort;
    private final DeleteCallInfoUseCase deleteCallInfoUseCase;

    @Override
    public void enqueue(QueueMessage message) {
        kafkaMessageProducer.send(message);
    }

    @Override
    public void processMessage(QueueMessage message) {
        // 메세지 처리
        if (!(message instanceof CallMessage)) {
            // 메세지가 콜 메세지가 아닌 경우 처리
            return;
        }

        CallMessage callMessage = (CallMessage) message;

        // 1. 출발 위치에서 10분 거리 내의 운행 가능한 택시 목록 찾기
        int radiusInKm = 5 * callMessage.getRetryCount();

        List<String> availableTaxiDrivers = findNearbyAvailableDriversPort.findNearByAvailableDrivers(callMessage.getCallId(), callMessage.getTaxiType(), callMessage.getExpectedOrigin().getLatitude(), callMessage.getExpectedOrigin().getLongitude(), radiusInKm);

        log.info("범위 내 유효한 택시의 수 : {}", availableTaxiDrivers.size());

        if (availableTaxiDrivers.isEmpty()) {
            // 여겨시 택시 수가 0인경우 없다는 메세지를 고객에게 전달.
            log.info("범위 내에 유효한 택시가 없음");
            deleteCallInfoUseCase.deleteCallById(callMessage.getCallId());
            notifyDispatchFailedToCustomerPort.sendToCustomer(callMessage.getCustomerLoginId(), new ErrorResponse("배차취소", "범위 내에 택시가 없습니다. 잠시 후에 시도해주세요."));
            throw new NoAvailableTaxiException();
        }

        // 2. 해당 택시기사들에게 메세지 전송
        setCallWithPort.setCallWith(callMessage.getCallId(), CallStatus.SENT);
        for (String taxiDriverLonginId : availableTaxiDrivers) {
            sendCallRequestToDriverPort.sendToDriver(taxiDriverLonginId, callMessage);
        }
        log.info("유효한 택시기사에게 콜 요청 전송 완료");
    }
}
