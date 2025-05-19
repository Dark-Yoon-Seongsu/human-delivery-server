package goorm.humandelivery.domain.model.entity;


import goorm.humandelivery.driver.domain.TaxiDriver;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
