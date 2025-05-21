package goorm.humandelivery.driving.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DrivingInfoResponse {

    private boolean isDrivingStarted;
    private boolean isDrivingFinished;

    public DrivingInfoResponse(boolean isDrivingStarted, boolean isDrivingFinished) {
        this.isDrivingStarted = isDrivingStarted;
        this.isDrivingFinished = isDrivingFinished;
    }
}
