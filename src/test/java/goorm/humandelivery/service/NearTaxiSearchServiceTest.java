package goorm.humandelivery.service;

import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.model.internal.CallMessage;
import goorm.humandelivery.infrastructure.redis.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

//./gradlew test --tests "goorm.humandelivery.service.NearTaxiSearchServiceTest"
@ExtendWith(MockitoExtension.class)
public class NearTaxiSearchServiceTest {

    @InjectMocks
    private NearTaxiSearchService nearTaxiSearchService;

    @Mock
    private RedisService redisService;

    private final double centerLat = 37.5665;
    private final double centerLon = 126.9780;
    private final double searchRadiusKm = 3.0;
    private final TaxiType taxiType = TaxiType.NORMAL;

    private CallMessage buildCallMessage(double lat, double lon, TaxiType type) {
        return CallMessage.builder()
                .callId(1L)
                .customerLoginId("user123")
                .expectedOrigin(new Location(lat, lon))
                .expectedDestination(new Location(lat + 0.01, lon + 0.01))
                .taxiType(type)
                .retryCount(1)
                .build();
    }

    private CallMessage buildCallMessage(double lat, double lon, TaxiType type, int retryCount) {
        return CallMessage.builder()
                .callId(1L)
                .customerLoginId("user123")
                .expectedOrigin(new Location(lat, lon))
                .expectedDestination(new Location(lat + 0.01, lon + 0.01))
                .taxiType(type)
                .retryCount(retryCount)
                .build();
    }

    @Test
    @DisplayName("TC01 - 반경 내 택시 존재 시: 근처 택시 목록 반환")
    void testFindNearbyTaxis_Success() {
        // given
        Set<String> driverIds = Set.of("driver1", "driver2", "driver3");
        CallMessage callMessage = buildCallMessage(centerLat, centerLon, taxiType);

        when(redisService.getActiveDrivers()).thenReturn(driverIds);
        when(redisService.getDriversTaxiType("driver1")).thenReturn(TaxiType.NORMAL);
        when(redisService.getDriversTaxiType("driver2")).thenReturn(TaxiType.NORMAL);
        when(redisService.getDriversTaxiType("driver3")).thenReturn(TaxiType.VENTI); // 타입 불일치

        when(redisService.getDriverLocation("driver1")).thenReturn(new Location(centerLat, centerLon));
        when(redisService.getDriverLocation("driver2")).thenReturn(new Location(centerLat + 0.01, centerLon + 0.01));
        // driver3는 무시

        // when
        List<String> nearbyDrivers = nearTaxiSearchService.findNearByAvailableDrivers(callMessage);

        // then
        assertThat(nearbyDrivers).containsExactlyInAnyOrder("driver1", "driver2");
    }

    @Test
    @DisplayName("TC02 - 반경 내 택시 미존재 시: 빈 목록 반환")
    void testFindNearbyTaxis_Failure_NoDrivers() {
        // given
        Set<String> driverIds = Set.of("driver1");
        CallMessage callMessage = buildCallMessage(centerLat, centerLon, taxiType);

        when(redisService.getActiveDrivers()).thenReturn(driverIds);
        when(redisService.getDriversTaxiType("driver1")).thenReturn(TaxiType.NORMAL);
        when(redisService.getDriverLocation("driver1"))
                .thenReturn(new Location(centerLat + 1.0, centerLon + 1.0)); // 반경 바깥

        // when
        List<String> nearbyDrivers = nearTaxiSearchService.findNearByAvailableDrivers(callMessage);

        // then
        assertThat(nearbyDrivers).isEmpty();
    }

    @Test
    @DisplayName("TC03 - active drivers가 null일 경우 빈 목록 반환")
    void testFindNearbyTaxis_NullActiveDrivers() {
        // given
        CallMessage callMessage = buildCallMessage(centerLat, centerLon, taxiType);
        when(redisService.getActiveDrivers()).thenReturn(null);

        // when
        List<String> nearbyDrivers = nearTaxiSearchService.findNearByAvailableDrivers(callMessage);

        // then
        assertThat(nearbyDrivers).isEmpty();
    }

    @Test
    @DisplayName("TC04 - driver 위치가 null일 경우 해당 드라이버 무시")
    void testFindNearbyTaxis_NullDriverLocation() {
        // given
        Set<String> driverIds = Set.of("driver1", "driver2");
        CallMessage callMessage = buildCallMessage(centerLat, centerLon, taxiType);

        when(redisService.getActiveDrivers()).thenReturn(driverIds);
        when(redisService.getDriversTaxiType("driver1")).thenReturn(TaxiType.NORMAL);
        when(redisService.getDriversTaxiType("driver2")).thenReturn(TaxiType.NORMAL);
        when(redisService.getDriverLocation("driver1")).thenReturn(null);
        when(redisService.getDriverLocation("driver2")).thenReturn(new Location(centerLat, centerLon));

        // when
        List<String> nearbyDrivers = nearTaxiSearchService.findNearByAvailableDrivers(callMessage);

        // then
        assertThat(nearbyDrivers).containsExactly("driver2");
    }

    @Test
    @DisplayName("TC05 - driver taxi type이 null일 경우 해당 드라이버 무시")
    void testFindNearbyTaxis_NullDriverTaxiType() {
        // given
        Set<String> driverIds = Set.of("driver1");
        CallMessage callMessage = buildCallMessage(centerLat, centerLon, taxiType);

        when(redisService.getActiveDrivers()).thenReturn(driverIds);
        when(redisService.getDriversTaxiType("driver1")).thenReturn(null);
        // 위치 stubbing 생략: 호출되지 않음

        // when
        List<String> nearbyDrivers = nearTaxiSearchService.findNearByAvailableDrivers(callMessage);

        // then
        assertThat(nearbyDrivers).isEmpty();
    }

    @Test
    @DisplayName("TC06 - 검색 반경이 0 이하일 경우 빈 목록 반환")
    void testFindNearbyTaxis_InvalidRadius() {
        // given
        CallMessage callMessage = buildCallMessage(centerLat, centerLon, taxiType, 0);
        CallMessage callMessage2 = buildCallMessage(centerLat, centerLon, taxiType, -1);

        // when
        List<String> nearbyDriversZero = nearTaxiSearchService.findNearByAvailableDrivers(callMessage);
        List<String> nearbyDriversNegative = nearTaxiSearchService.findNearByAvailableDrivers(callMessage2);

        // then
        assertThat(nearbyDriversZero).isEmpty();
        assertThat(nearbyDriversNegative).isEmpty();
    }
}
