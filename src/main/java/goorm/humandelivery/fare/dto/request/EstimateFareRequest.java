package goorm.humandelivery.fare.dto.request;

import goorm.humandelivery.shared.location.domain.Location;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstimateFareRequest {

    @NotNull(message = "출발 위치를 입력해 주세요.")
    private Location expectedOrigin;

    @NotNull(message = "도착 위치를 입력해 주세요.")
    private Location expectedDestination;

}
