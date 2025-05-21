package goorm.humandelivery.call.domain;

import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Matching extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id")
    private CallInfo callInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxi_driver_id")
    private TaxiDriver taxiDriver;

    @Builder
    private Matching(Long id, CallInfo callInfo, TaxiDriver taxiDriver) {
        this.id = id;
        this.callInfo = callInfo;
        this.taxiDriver = taxiDriver;
    }


}
