package goorm.humandelivery.driver.dto.request;

import goorm.humandelivery.shared.location.domain.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateDriverLocationRequest {

    private String customerLoginId;

    @Valid
    @NotNull
    private Location location;


    public UpdateDriverLocationRequest(String customerLoginId, Location location) {
        this.customerLoginId = customerLoginId;
        this.location = location;
    }
}
