package goorm.humandelivery.driver.domain;

import goorm.humandelivery.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Taxi extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taxi_id")
    private Long id;

    private String model;

    @Enumerated(value = EnumType.STRING)
    private TaxiType taxiType;

    private String plateNumber;

    @Enumerated(value = EnumType.STRING)
    private FuelType fuelType;

    @Builder
    private Taxi(String model, TaxiType taxiType, String plateNumber, FuelType fuelType) {
        this.model = model;
        this.taxiType = taxiType;
        this.plateNumber = plateNumber;
        this.fuelType = fuelType;
    }
}
