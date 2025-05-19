package goorm.humandelivery.domain.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallAcceptRequest {

    @NotBlank
    private Long callId;

}
