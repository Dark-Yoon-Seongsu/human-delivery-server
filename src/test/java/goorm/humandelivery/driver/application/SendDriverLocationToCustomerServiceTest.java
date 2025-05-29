package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.domain.TaxiDriverStatus;
import goorm.humandelivery.driver.domain.TaxiType;
import goorm.humandelivery.driver.dto.response.DriverLocationResponse;
import goorm.humandelivery.global.exception.CustomerNotAssignedException;
import goorm.humandelivery.global.exception.OffDutyLocationUpdateException;
import goorm.humandelivery.shared.location.domain.Location;
import goorm.humandelivery.shared.application.port.out.SetValueWithTtlPort;
import goorm.humandelivery.shared.location.application.port.out.SetLocationPort;
import goorm.humandelivery.driver.application.port.out.SendDriverLocationToCustomerPort;
import goorm.humandelivery.shared.redis.RedisKeyParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//./gradlew test --tests "goorm.humandelivery.driver.application.SendDriverLocationToCustomerServiceTest"
@ExtendWith(MockitoExtension.class)
class SendDriverLocationToCustomerServiceTest {

    @Mock
    private SetLocationPort setLocationPort;

    @Mock
    private SetValueWithTtlPort setValueWithTtlPort;

    @Mock
    private SendDriverLocationToCustomerPort sendToCustomerPort;

    @InjectMocks
    private SendDriverLocationToCustomerService service;

    private final String taxiDriverLoginId = "driver1";
    private final TaxiType taxiType = TaxiType.NORMAL;
    private final Location location = new Location(37.123, 127.456);

    @BeforeEach
    void setup() {

    }

    @Test
    void sendLocation_offDuty_throwsException() {
        TaxiDriverStatus status = TaxiDriverStatus.OFF_DUTY;

        OffDutyLocationUpdateException ex = assertThrows(OffDutyLocationUpdateException.class,
                () -> service.sendLocation(taxiDriverLoginId, status, taxiType, "customer1", location));

        assertNotNull(ex);
        verifyNoInteractions(setLocationPort, setValueWithTtlPort, sendToCustomerPort);
    }

    @Test
    void sendLocation_available_savesLocationAndUpdateTime() {
        TaxiDriverStatus status = TaxiDriverStatus.AVAILABLE;
        String expectedLocationKey = RedisKeyParser.getTaxiDriverLocationKeyBy(status, taxiType);
        String expectedUpdateTimeKey = RedisKeyParser.taxiDriverLastUpdate(taxiDriverLoginId);

        service.sendLocation(taxiDriverLoginId, status, taxiType, null, location);

        verify(setLocationPort).setLocation(eq(expectedLocationKey), eq(taxiDriverLoginId), eq(location));
        verify(setValueWithTtlPort).setValueWithTTL(eq(expectedUpdateTimeKey), anyString(), eq(Duration.ofMinutes(5)));
        verifyNoInteractions(sendToCustomerPort);
    }


    @Test
    void sendLocation_reserved_withCustomer_sendsLocationToCustomer() {
        TaxiDriverStatus status = TaxiDriverStatus.RESERVED;
        String customerLoginId = "customer1";
        String expectedLocationKey = RedisKeyParser.getTaxiDriverLocationKeyBy(status, taxiType);

        service.sendLocation(taxiDriverLoginId, status, taxiType, customerLoginId, location);

        verify(setLocationPort).setLocation(eq(expectedLocationKey), eq(taxiDriverLoginId), eq(location));
        verify(setValueWithTtlPort).setValueWithTTL(anyString(), anyString(), eq(Duration.ofMinutes(5)));

        ArgumentCaptor<DriverLocationResponse> captor = ArgumentCaptor.forClass(DriverLocationResponse.class);
        verify(sendToCustomerPort).sendToCustomer(eq(customerLoginId), captor.capture());

        DriverLocationResponse actualResponse = captor.getValue();
        assertEquals(location, actualResponse.getLocation());
    }



    @Test
    void sendLocation_reserved_withoutCustomer_throwsException() {
        TaxiDriverStatus status = TaxiDriverStatus.RESERVED;

        CustomerNotAssignedException ex = assertThrows(CustomerNotAssignedException.class,
                () -> service.sendLocation(taxiDriverLoginId, status, taxiType, null, location));

        assertNotNull(ex);

    }


    @Test
    void sendLocation_onDriving_withCustomer_sendsLocationToCustomer() {
        TaxiDriverStatus status = TaxiDriverStatus.ON_DRIVING;
        String customerLoginId = "customer1";
        String expectedLocationKey = RedisKeyParser.getTaxiDriverLocationKeyBy(status, taxiType);

        service.sendLocation(taxiDriverLoginId, status, taxiType, customerLoginId, location);

        verify(setLocationPort).setLocation(eq(expectedLocationKey), eq(taxiDriverLoginId), eq(location));
        verify(setValueWithTtlPort).setValueWithTTL(anyString(), anyString(), eq(Duration.ofMinutes(5)));
        verify(sendToCustomerPort).sendToCustomer(eq(customerLoginId), any(DriverLocationResponse.class));
    }

    @Test
    void sendLocation_onDriving_withoutCustomer_throwsException() {
        TaxiDriverStatus status = TaxiDriverStatus.ON_DRIVING;
        String expectedLocationKey = RedisKeyParser.getTaxiDriverLocationKeyBy(status, taxiType);

        CustomerNotAssignedException ex = assertThrows(CustomerNotAssignedException.class,
                () -> service.sendLocation(taxiDriverLoginId, status, taxiType, null, location));

        assertNotNull(ex);
        verify(setLocationPort).setLocation(eq(expectedLocationKey), eq(taxiDriverLoginId), eq(location));
        verify(setValueWithTtlPort).setValueWithTTL(anyString(), anyString(), eq(Duration.ofMinutes(5)));
        verifyNoInteractions(sendToCustomerPort);
    }
}
