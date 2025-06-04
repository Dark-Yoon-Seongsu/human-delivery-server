package goorm.humandelivery.driver.domain;

import goorm.humandelivery.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter

public class TaxiDriver extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taxi_driver_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxi_id")
    private Taxi taxi;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String licenseCode;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(value = EnumType.STRING)
    private TaxiDriverStatus status;

    @Builder(toBuilder = true)
    private TaxiDriver(Taxi taxi, String loginId, String password, String name, String licenseCode, String phoneNumber, TaxiDriverStatus status) {
        this.taxi = taxi;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.licenseCode = licenseCode;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }

    public TaxiDriverStatus changeStatus(TaxiDriverStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("상태는 null일 수 없습니다.");
        }
        this.status = status;
        return this.status;
    }
}
