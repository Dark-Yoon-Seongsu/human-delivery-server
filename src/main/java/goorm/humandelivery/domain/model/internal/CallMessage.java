package goorm.humandelivery.domain.model.internal;

import goorm.humandelivery.domain.model.entity.Customer;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CallMessage extends QueueMessage {

	private Long callId;
	private String customerLoginId;
	private Location expectedOrigin;
	private Location expectedDestination;
	private TaxiType taxiType;
	private Integer retryCount;

}
