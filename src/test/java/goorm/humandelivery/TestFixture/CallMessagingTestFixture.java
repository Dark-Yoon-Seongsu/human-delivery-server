package goorm.humandelivery.TestFixture;

import goorm.humandelivery.domain.model.entity.Customer;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.request.CallMessageRequest;

public class CallMessagingTestFixture {
    public static Customer buildCustomer(String loginId) {
        return Customer.builder()
                .loginId(loginId)
                .password("test-password")
                .name("테스트 고객")
                .phoneNumber("010-1234-5678")
                .build();
    }

    public static Location buildLocation(double lat, double lon) {
        return Location.builder()
                .latitude(lat)
                .longitude(lon)
                .build();
    }

    public static CallMessageRequest buildCallMessageRequest(
            Location origin,
            Location destination,
            TaxiType taxiType,
            int retryCount
    ) {
        return new CallMessageRequest(origin, destination, taxiType, retryCount);
    }

    public static CallMessageRequest buildDefaultCallMessageRequest() {
        return buildCallMessageRequest(
                buildLocation(37.5, 127.0),
                buildLocation(37.6, 127.1),
                TaxiType.NORMAL,
                0
        );
    }
}
