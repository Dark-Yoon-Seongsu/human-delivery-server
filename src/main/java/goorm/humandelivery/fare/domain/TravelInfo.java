package goorm.humandelivery.fare.domain;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TravelInfo {

    private final double distanceMeters;
    private final int durationSeconds;
    private final BigDecimal estimatedFareByKakao;

    public TravelInfo(double distanceMeters, int durationSeconds, BigDecimal estimatedFareByKakao) {
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
        this.estimatedFareByKakao = estimatedFareByKakao;
    }

}