package goorm.humandelivery.call.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallRejectRequest {

    @NotBlank
    private Long callId;

}
