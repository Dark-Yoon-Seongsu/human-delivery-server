package goorm.humandelivery.driving.dto.response;

import goorm.humandelivery.driving.domain.DrivingStatus;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class DrivingSummaryResponse {

    private Long callId;
    private String customerLoginId;
    private String taxiDriverLoginId;

    private Location origin;
    private LocalDateTime pickupTime;

    private Location destination;
    private LocalDateTime arrivingTime;
    private DrivingStatus drivingStatus;
    private boolean reported;

    public DrivingSummaryResponse(Long callId, String customerLoginId, String taxiDriverLoginId, Location origin,
                                  LocalDateTime pickupTime, Location destination, LocalDateTime arrivingTime, DrivingStatus drivingStatus,
                                  boolean reported) {
        this.callId = callId;
        this.customerLoginId = customerLoginId;
        this.taxiDriverLoginId = taxiDriverLoginId;
        this.origin = origin;
        this.pickupTime = pickupTime;
        this.destination = destination;
        this.arrivingTime = arrivingTime;
        this.drivingStatus = drivingStatus;
        this.reported = reported;
    }
}
