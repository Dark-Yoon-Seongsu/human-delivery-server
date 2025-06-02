package goorm.humandelivery.fare.infrastructure.kakao;

import goorm.humandelivery.fare.domain.TravelInfo;
import goorm.humandelivery.global.exception.InvalidRouteException;
import goorm.humandelivery.shared.location.domain.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class KakaoTravelInfoAdapterTest {

    @Autowired
    KakaoTravelInfoAdapter kakaoTravelInfoAdapter;

    @Test
    @DisplayName("출발지와 목적지로 예상 거리, 예상 소요 시간, 예상 카카오 요금을 확인할 수 있다.")
    void loadTravelInfo() throws Exception {
        // Given
        Location origin = new Location(37.5665, 126.9780); // 서울시청
        Location destination = new Location(37.4979, 127.0276); // 강남역

        // When
        TravelInfo travelInfo = kakaoTravelInfoAdapter.loadTravelInfo(origin, destination);

        // Then
        assertThat(travelInfo.getDistanceMeters()).isGreaterThan(1000);
        assertThat(travelInfo.getEstimatedFareByKakao()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("출발지와 도착지가 5m 이내로 설정된 경우 예외가 발생한다.")
    void loadTravelInfoWithCode104() throws Exception {
        // Given
        Location origin = new Location(37.5665, 126.9780); // 서울시청
        Location destination = new Location(37.5665, 126.9780); // 서울시청

        // When
        // Then
        assertThatThrownBy(() -> kakaoTravelInfoAdapter.loadTravelInfo(origin, destination))
                .isInstanceOf(InvalidRouteException.class)
                .hasMessage("카카오 경로 탐색 실패 (msg: 출발지와 도착지가 5 m 이내로 설정된 경우 경로를 탐색할 수 없음)");

    }
}