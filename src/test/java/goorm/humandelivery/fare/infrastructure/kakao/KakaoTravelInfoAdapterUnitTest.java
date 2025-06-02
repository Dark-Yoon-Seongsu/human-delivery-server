package goorm.humandelivery.fare.infrastructure.kakao;

import goorm.humandelivery.fare.infrastructure.kakao.dto.KakaoDirectionResponse;
import goorm.humandelivery.shared.location.domain.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KakaoTravelInfoAdapterUnitTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    KakaoTravelInfoAdapter kakaoTravelInfoAdapter;

    @Test
    @DisplayName("카카오 API 호출 시 예외가 발생한다.")
    void loadTravelInfoWithRestClientException() throws Exception {
        // Given
        Location origin = new Location(37.5665, 126.9780); // 서울시청
        Location destination = new Location(37.4979, 127.0276); // 강남역
        given(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(KakaoDirectionResponse.class))).willThrow(new RestClientException("카카오 API 호출 실패"));

        // When
        // Then
        assertThatThrownBy(() -> kakaoTravelInfoAdapter.loadTravelInfo(origin, destination))
                .isInstanceOf(RestClientException.class)
                .hasMessage("카카오 API 호출 실패");
    }
}