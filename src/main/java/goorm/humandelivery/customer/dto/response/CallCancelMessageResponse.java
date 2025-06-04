package goorm.humandelivery.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CallCancelMessageResponse {
    private String message;
    private Long callId;
}
