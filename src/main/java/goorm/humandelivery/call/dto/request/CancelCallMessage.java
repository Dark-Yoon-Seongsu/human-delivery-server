package goorm.humandelivery.call.dto.request;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelCallMessage {
    private Long callId;


    public CancelCallMessage() {
        // 기본 생성자
    }

    public CancelCallMessage(Long id) {
        this.callId = id;
    }
}
