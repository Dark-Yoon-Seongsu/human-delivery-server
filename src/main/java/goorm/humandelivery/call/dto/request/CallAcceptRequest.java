package goorm.humandelivery.call.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallAcceptRequest {

    @NotNull(message = "콜 ID는 필수입니다.")
    private Long callId;

}
