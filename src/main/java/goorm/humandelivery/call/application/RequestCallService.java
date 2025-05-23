package goorm.humandelivery.call.application;

import goorm.humandelivery.call.application.port.in.RequestCallUseCase;
import goorm.humandelivery.call.application.port.out.SaveCallInfoPort;
import goorm.humandelivery.call.dto.request.CallMessageRequest;
import goorm.humandelivery.customer.application.port.out.LoadCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.exception.CustomerNotFoundException;
import goorm.humandelivery.shared.application.port.out.MessageQueuePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 책임: 웹소켓 연결 및 클라이언트와의 상호작용을 담당하는 서비스
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestCallService implements RequestCallUseCase {

    private final LoadCustomerPort loadCustomerPort;
    private final SaveCallInfoPort saveCallInfoPort;
    private final MessageQueuePort messageQueuePort;

    @Override
    public void requestCall(CallMessageRequest request, String customerLoginId) {
        Long callId = saveCallAndGetCallId(request, customerLoginId);
        log.info("콜 내용 DB에 저장 완료");
        messageQueuePort.enqueue(request.toQueueMessage(callId, customerLoginId));
        log.info("콜 요청을 카프카 메시지 큐에 등록");
    }

    private Long saveCallAndGetCallId(CallMessageRequest request, String customerLoginId) {
        Customer customer = loadCustomerPort.findByLoginId(customerLoginId)
                .orElseThrow(CustomerNotFoundException::new);
        return saveCallInfoPort.save(request.toCallInfo(customer)).getId();
    }
}
