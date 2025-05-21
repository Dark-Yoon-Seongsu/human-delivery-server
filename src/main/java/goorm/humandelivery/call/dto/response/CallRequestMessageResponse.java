package goorm.humandelivery.call.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallRequestMessageResponse {
    private String message;
    // 성공 실패 여부 Enum으로 추가

    public CallRequestMessageResponse(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
