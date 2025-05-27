package goorm.humandelivery.call.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallRejectRequest {

    @NotNull
    private Long callId;

}
