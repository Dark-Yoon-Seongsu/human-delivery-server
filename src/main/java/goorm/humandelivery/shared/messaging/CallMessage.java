package goorm.humandelivery.shared.messaging;

import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CallMessage extends QueueMessage {

    private Long callId;
    private String customerLoginId;
    private Location expectedOrigin;
    private Location expectedDestination;
    private TaxiType taxiType;
    private Integer retryCount;

}
