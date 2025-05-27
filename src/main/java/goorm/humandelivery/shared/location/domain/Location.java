package goorm.humandelivery.shared.location.domain;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    @NotNull
    private Double latitude;   // 위도

    @NotNull
    private Double longitude;  // 경도

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(getLatitude(), location.getLatitude()) && Objects.equals(getLongitude(), location.getLongitude());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLatitude(), getLongitude());
    }
}
