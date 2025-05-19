package goorm.humandelivery.domain.model.request;

import goorm.humandelivery.domain.model.entity.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateLocationRequest {

    private String customerLoginId;

    @Valid
    @NotNull
    private Location location;


}
