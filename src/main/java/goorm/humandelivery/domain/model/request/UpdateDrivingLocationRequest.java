package goorm.humandelivery.domain.model.request;

import goorm.humandelivery.domain.model.entity.Location;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDrivingLocationRequest {

    @NotBlank
    private String customerLoginId;

    @NotBlank
    private Location location;
}


