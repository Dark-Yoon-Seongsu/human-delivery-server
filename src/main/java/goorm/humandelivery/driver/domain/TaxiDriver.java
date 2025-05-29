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

    @Column(unique = true)
    private String loginId;

    private String password;

    private String name;

    private String licenseCode;

    @NotBlank
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-XXXX-XXXX 이어야 합니다.")
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
