package goorm.humandelivery.fare.infrastructure.kakao;

import goorm.humandelivery.fare.application.port.out.LoadTravelInfoPort;
import goorm.humandelivery.fare.domain.TravelInfo;
import goorm.humandelivery.fare.infrastructure.kakao.dto.KakaoDirectionResponse;
import goorm.humandelivery.fare.infrastructure.kakao.dto.Route;
import goorm.humandelivery.global.exception.InvalidRouteException;
import goorm.humandelivery.shared.location.domain.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoTravelInfoAdapter implements LoadTravelInfoPort {

    private static final String KAKAO_DIRECTION_URL = "https://apis-navi.kakaomobility.com/v1/directions";
    private final RestTemplate restTemplate;

    @Value("${kakao.rest.api.key}")
    private String kakaoApiKey;

    @Override
    public TravelInfo loadTravelInfo(Location origin, Location destination) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(KAKAO_DIRECTION_URL)
                    .queryParam("origin", origin.getLongitude() + "," + origin.getLatitude())
                    .queryParam("destination", destination.getLongitude() + "," + destination.getLatitude())
                    .queryParam("priority", "RECOMMEND")
                    .build()
                    .toUri();

            log.info("[KakaoTravelInfoAdapter.loadTravelInfo] request URI = {}", uri);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<Object> entity = new HttpEntity<>(headers);

            ResponseEntity<KakaoDirectionResponse> response = restTemplate.exchange(uri, HttpMethod.GET, entity, KakaoDirectionResponse.class);

            Route route = response.getBody().getRoutes().get(0);
            int resultCode = route.getResultCode();
            if (resultCode != 0) {
                log.error("[resultCode = {}, 경로 탐색 실패]", resultCode);
                throw new InvalidRouteException(resultCode, "카카오 경로 탐색 실패 (msg: " + route.getResultMsg() + ")");
            }

            double distance = route.getSummary().getDistance();
            int duration = route.getSummary().getDuration();
            int fare = route.getSummary().getFare().getTaxi();
            log.info("[resultCode = {}, 경로 탐색 성공] 예상거리={}, 예상소요시간={}, 예상카카오요금={}", resultCode, distance, duration, fare);
            return new TravelInfo(distance, duration, BigDecimal.valueOf(fare));
        } catch (RestClientException e) {
            log.error("카카오 API 호출 중 예외 발생: {}", e.getMessage(), e);
            throw new RestClientException("카카오 API 호출 실패", e);
        }
    }

}
