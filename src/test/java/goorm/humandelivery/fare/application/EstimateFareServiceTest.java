package goorm.humandelivery.fare.application;

import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.fare.application.port.out.LoadTravelInfoPort;
import goorm.humandelivery.fare.domain.TravelInfo;
import goorm.humandelivery.fare.dto.request.EstimateFareRequest;
import goorm.humandelivery.fare.dto.response.EstimateFareResponse;
import goorm.humandelivery.shared.location.domain.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class EstimateFareServiceTest {

    @Autowired
    EstimateFareService estimateFareService;

    @MockitoBean
    LoadTravelInfoPort loadTravelInfoPort;

    @Test
    @DisplayName("주간에 출발지와 목적지를 통해 예상 요금을 확인할 수 있다.")
    void estimateFare() {
        Location origin = new Location(37.5665, 126.9780);
        Location destination = new Location(37.4979, 127.0276);
        EstimateFareRequest request = new EstimateFareRequest(origin, destination, LocalTime.of(15, 0));
        TravelInfo travelInfo = new TravelInfo(8000, 1200, BigDecimal.valueOf(10200));
        given(loadTravelInfoPort.loadTravelInfo(origin, destination)).willReturn(travelInfo);

        EstimateFareResponse response = estimateFareService.estimateFare(request);

        assertThat(response.getEstimatedFares().get(TaxiType.NORMAL)).isEqualTo(BigDecimal.valueOf(9600));
        assertThat(response.getEstimatedFares().get(TaxiType.PREMIUM)).isEqualTo(BigDecimal.valueOf(13600));
        assertThat(response.getEstimatedFares().get(TaxiType.VENTI)).isEqualTo(BigDecimal.valueOf(13600));
        assertThat(response.getKakaoEstimatedFare()).isEqualTo(BigDecimal.valueOf(10200));
    }

    @Test
    @DisplayName("21:59:59의 예상 요금은 심야요금을 미적용한 요금이다.")
    void estimateFareWithBeforeNightStart() {
        Location origin = new Location(37.5665, 126.9780);
        Location destination = new Location(37.4979, 127.0276);
        EstimateFareRequest request = new EstimateFareRequest(origin, destination, LocalTime.of(21, 59, 59));
        TravelInfo travelInfo = new TravelInfo(8000, 1200, BigDecimal.valueOf(10200));
        given(loadTravelInfoPort.loadTravelInfo(origin, destination)).willReturn(travelInfo);

        EstimateFareResponse response = estimateFareService.estimateFare(request);

        assertThat(response.getEstimatedFares().get(TaxiType.NORMAL)).isEqualTo(BigDecimal.valueOf(9600));
        assertThat(response.getEstimatedFares().get(TaxiType.PREMIUM)).isEqualTo(BigDecimal.valueOf(13600));
        assertThat(response.getEstimatedFares().get(TaxiType.VENTI)).isEqualTo(BigDecimal.valueOf(13600));
    }

    @Test
    @DisplayName("22:00:00의 예상 요금은 심야요금을 적용한 요금이다.")
    void estimateFareWithAfterNightStart() {
        Location origin = new Location(37.5665, 126.9780);
        Location destination = new Location(37.4979, 127.0276);
        EstimateFareRequest request = new EstimateFareRequest(origin, destination, LocalTime.of(22, 0));
        TravelInfo travelInfo = new TravelInfo(8000, 1200, BigDecimal.valueOf(10200));
        given(loadTravelInfoPort.loadTravelInfo(origin, destination)).willReturn(travelInfo);

        EstimateFareResponse response = estimateFareService.estimateFare(request);

        assertThat(response.getEstimatedFares().get(TaxiType.NORMAL)).isEqualTo(BigDecimal.valueOf(11560));
        assertThat(response.getEstimatedFares().get(TaxiType.PREMIUM)).isEqualTo(BigDecimal.valueOf(16320));
        assertThat(response.getEstimatedFares().get(TaxiType.VENTI)).isEqualTo(BigDecimal.valueOf(16320));
    }

    @Test
    @DisplayName("23:00:00의 예상 요금은 심야피크요금을 적용한 요금이다.")
    void estimateFareAtDeepNightStart() {
        Location origin = new Location(37.5665, 126.9780);
        Location destination = new Location(37.4979, 127.0276);
        EstimateFareRequest request = new EstimateFareRequest(origin, destination, LocalTime.of(23, 0));
        TravelInfo travelInfo = new TravelInfo(8000, 1200, BigDecimal.valueOf(10200));
        given(loadTravelInfoPort.loadTravelInfo(origin, destination)).willReturn(travelInfo);

        EstimateFareResponse response = estimateFareService.estimateFare(request);

        assertThat(response.getEstimatedFares().get(TaxiType.NORMAL)).isEqualTo(BigDecimal.valueOf(13420));
        assertThat(response.getEstimatedFares().get(TaxiType.PREMIUM)).isEqualTo(BigDecimal.valueOf(16320));
        assertThat(response.getEstimatedFares().get(TaxiType.VENTI)).isEqualTo(BigDecimal.valueOf(16320));
    }

    @Test
    @DisplayName("01:59:59의 예상 요금은 심야피크요금을 적용한 요금이다.")
    void estimateFareNearDeepNightEnd() {
        Location origin = new Location(37.5665, 126.9780);
        Location destination = new Location(37.4979, 127.0276);
        EstimateFareRequest request = new EstimateFareRequest(origin, destination, LocalTime.of(1, 59, 59));
        TravelInfo travelInfo = new TravelInfo(8000, 1200, BigDecimal.valueOf(10200));
        given(loadTravelInfoPort.loadTravelInfo(origin, destination)).willReturn(travelInfo);

        EstimateFareResponse response = estimateFareService.estimateFare(request);

        assertThat(response.getEstimatedFares().get(TaxiType.NORMAL)).isEqualTo(BigDecimal.valueOf(13420));
        assertThat(response.getEstimatedFares().get(TaxiType.PREMIUM)).isEqualTo(BigDecimal.valueOf(16320));
        assertThat(response.getEstimatedFares().get(TaxiType.VENTI)).isEqualTo(BigDecimal.valueOf(16320));
    }

    @Test
    @DisplayName("03:59:59의 예상 요금은 심야요금을 적용한 요금이다.")
    void estimateFareWithBeforeNightEnd() {
        Location origin = new Location(37.5665, 126.9780);
        Location destination = new Location(37.4979, 127.0276);
        EstimateFareRequest request = new EstimateFareRequest(origin, destination, LocalTime.of(3, 59, 59));
        TravelInfo travelInfo = new TravelInfo(8000, 1200, BigDecimal.valueOf(10200));
        given(loadTravelInfoPort.loadTravelInfo(origin, destination)).willReturn(travelInfo);

        EstimateFareResponse response = estimateFareService.estimateFare(request);

        assertThat(response.getEstimatedFares().get(TaxiType.NORMAL)).isEqualTo(BigDecimal.valueOf(11560));
        assertThat(response.getEstimatedFares().get(TaxiType.PREMIUM)).isEqualTo(BigDecimal.valueOf(16320));
        assertThat(response.getEstimatedFares().get(TaxiType.VENTI)).isEqualTo(BigDecimal.valueOf(16320));
    }

    @Test
    @DisplayName("04:00:00의 예상 요금은 심야요금을 미적용한 요금이다.")
    void estimateFareWithAfterNightEnd() {
        Location origin = new Location(37.5665, 126.9780);
        Location destination = new Location(37.4979, 127.0276);
        EstimateFareRequest request = new EstimateFareRequest(origin, destination, LocalTime.of(4, 0));
        TravelInfo travelInfo = new TravelInfo(8000, 1200, BigDecimal.valueOf(10200));
        given(loadTravelInfoPort.loadTravelInfo(origin, destination)).willReturn(travelInfo);

        EstimateFareResponse response = estimateFareService.estimateFare(request);

        assertThat(response.getEstimatedFares().get(TaxiType.NORMAL)).isEqualTo(BigDecimal.valueOf(9600));
        assertThat(response.getEstimatedFares().get(TaxiType.PREMIUM)).isEqualTo(BigDecimal.valueOf(13600));
        assertThat(response.getEstimatedFares().get(TaxiType.VENTI)).isEqualTo(BigDecimal.valueOf(13600));
    }
}
