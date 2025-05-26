package goorm.humandelivery.call.dto.response;

import goorm.humandelivery.call.domain.CallInfo;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.Builder;
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

    @Builder
    public CallAcceptResponse(Long callId, String customerName, String customerLoginId, String customerPhoneNumber,
                              Location expectedOrigin, Location expectedDestination) {
        this.callId = callId;
        this.customerName = customerName;
        this.customerLoginId = customerLoginId;
        this.customerPhoneNumber = customerPhoneNumber;
        this.expectedOrigin = expectedOrigin;
        this.expectedDestination = expectedDestination;
    }

    public static CallAcceptResponse from(CallInfo callInfo) {
        return CallAcceptResponse.builder()
                .callId(callInfo.getId())
                .customerName(callInfo.getCustomer().getName())
                .customerLoginId(callInfo.getCustomer().getLoginId())
                .customerPhoneNumber(callInfo.getCustomer().getPhoneNumber())
                .expectedOrigin(callInfo.getExpectedOrigin())
                .expectedDestination(callInfo.getExpectedDestination())
                .build();
    }
}
