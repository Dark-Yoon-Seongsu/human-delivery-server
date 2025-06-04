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


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status = CallStatus.SENT;


    public CallInfo(Long id, Customer customer, Location expectedOrigin, Location expectedDestination, TaxiType taxiType) {
        this.id = id;
        this.customer = customer;
        this.expectedOrigin = expectedOrigin;
        this.expectedDestination = expectedDestination;
        this.taxiType = taxiType;

    }


    public void cancel() {
        if (this.status == CallStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 콜입니다..");
        }
        this.status = CallStatus.CANCELLED;
    }

    public void match() {
        this.status = CallStatus.MATCHED;
    }

    public void complete() {
        this.status = CallStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return this.status == CallStatus.CANCELLED;
    }

    public boolean isWaiting() {
        return this.status == CallStatus.SENT;
    }

    public boolean isMatched() {
        return this.status == CallStatus.MATCHED;
    }

    public boolean isCompleted() {
        return this.status == CallStatus.COMPLETED;
    }

}
