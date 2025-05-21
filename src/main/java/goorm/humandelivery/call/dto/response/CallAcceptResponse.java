package goorm.humandelivery.call.dto.response;

import goorm.humandelivery.shared.location.domain.Location;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CallAcceptResponse {

    private Long callId;
    private String customerName;
    private String customerLoginId;
    private String customerPhoneNumber;
    private Location expectedOrigin;
    private Location expectedDestination;

    public CallAcceptResponse(Long callId, String customerName, String customerLoginId, String customerPhoneNumber,
                              Location expectedOrigin, Location expectedDestination) {
        this.callId = callId;
        this.customerName = customerName;
        this.customerLoginId = customerLoginId;
        this.customerPhoneNumber = customerPhoneNumber;
        this.expectedOrigin = expectedOrigin;
        this.expectedDestination = expectedDestination;
    }
}
