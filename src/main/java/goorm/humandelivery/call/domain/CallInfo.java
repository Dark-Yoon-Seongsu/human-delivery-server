package goorm.humandelivery.call.domain;

import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.shared.domain.BaseEntity;
import goorm.humandelivery.shared.location.domain.Location;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CallInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "call_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Embedded
    @AttributeOverride(name = "latitude", column = @Column(name = "ex_origin_latitude"))
    @AttributeOverride(name = "longitude", column = @Column(name = "ex_origin_longitude"))
    private Location expectedOrigin;


    @Embedded
    @AttributeOverride(name = "latitude", column = @Column(name = "ex_dest_latitude"))
    @AttributeOverride(name = "longitude", column = @Column(name = "ex_dest_longitude"))
    private Location expectedDestination;

    @Enumerated(value = EnumType.STRING)
    private TaxiType taxiType;

}
