package goorm.humandelivery.application;

import org.springframework.stereotype.Service;

import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.domain.model.request.CallMessageRequest;
import goorm.humandelivery.domain.repository.CallInfoRepository;
import goorm.humandelivery.infrastructure.messaging.KafkaMessageQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 책임: 웹소켓 연결 및 클라이언트와의 상호작용을 담당하는 서비스
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketCustomerService {

	private final CallInfoRepository callRepository;
	private final CustomerService customerService;

	// private final BlockingMessageQueueService messageQueueService;
	private final KafkaMessageQueueService messageQueueService;

	public void processMessage(CallMessageRequest request, String senderId) {
		Long callId = saveCallAndGetCallId(request, senderId);
		log.info("콜 내용 DB에 저장 완료");
		messageQueueService.enqueue(request.toQueueMessage(callId, senderId));
		log.info("콜 요청을 카프카 메시지 큐에 등록");
	}

	public Long saveCallAndGetCallId(CallMessageRequest request, String senderId) {
		Customer customer = customerService.findCustomerByLoginId(senderId);
		return callRepository.save(request.toCallInfo(customer)).getId();
	}

	public void deleteCallById(Long callId) {
		callRepository.deleteById(callId);
	}

}
