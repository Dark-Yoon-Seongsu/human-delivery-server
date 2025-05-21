package goorm.humandelivery.driving.domain;

import goorm.humandelivery.call.domain.Matching;
import goorm.humandelivery.shared.domain.BaseEntity;
import goorm.humandelivery.shared.location.domain.Location;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DrivingInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driving_info_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching;

    @Embedded
    @AttributeOverride(name = "latitude", column = @Column(name = "origin_latitude"))
    @AttributeOverride(name = "longitude", column = @Column(name = "origin_longitude"))

    private Location origin;

    private LocalDateTime pickupTime;

    @Embedded
    @AttributeOverride(name = "latitude", column = @Column(name = "dest_latitude"))
    @AttributeOverride(name = "longitude", column = @Column(name = "dest_longitude"))
    private Location destination;

    private LocalDateTime arrivingTime;

    @Enumerated(value = EnumType.STRING)
    private DrivingStatus drivingStatus;

    private boolean reported;

    @Builder
    public DrivingInfo(Matching matching, Location origin, LocalDateTime pickupTime, Location destination,
                       LocalDateTime arrivingTime, DrivingStatus drivingStatus, boolean reported) {
        this.matching = matching;
        this.origin = origin;
        this.pickupTime = pickupTime;
        this.destination = destination;
        this.arrivingTime = arrivingTime;
        this.drivingStatus = drivingStatus;
        this.reported = reported;
    }

    public boolean isDrivingStarted() {
        return drivingStatus == DrivingStatus.ON_DRIVING;
    }

    public boolean isDrivingFinished() {
        return drivingStatus == DrivingStatus.COMPLETE;
    }

    public void finishDriving(Location destination, LocalDateTime arrivingTime) {
        this.destination = destination;
        this.arrivingTime = arrivingTime;
        drivingStatus = DrivingStatus.COMPLETE;
    }

}
