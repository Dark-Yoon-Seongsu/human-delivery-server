package goorm.humandelivery.service;

import goorm.humandelivery.customer.service.CallSaveService;
import goorm.humandelivery.domain.model.entity.CallInfo;
import goorm.humandelivery.domain.model.entity.Customer;
import goorm.humandelivery.domain.model.entity.Location;
import goorm.humandelivery.domain.model.entity.TaxiType;
import goorm.humandelivery.domain.repository.CallInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

//./gradlew test --tests "goorm.humandelivery.service.CallSaveServiceTest"
class CallSaveServiceTest {

    @Mock
    private CallInfoRepository callInfoRepository;

    @InjectMocks
    private CallSaveService callSaveService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("TC01 호출 등록 성공시 테스트")
    void TC01testSaveCallInfo_Success() {
        // Given
        Customer customer = Customer.builder()
                .loginId("testuser")
                .password("securepassword")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();

        Location expectedOrigin = new Location(37.5665, 126.9780);
        Location expectedDestination = new Location(37.5651, 126.9895);
        TaxiType taxiType = TaxiType.NORMAL;

        CallInfo callInfo = CallInfo.builder()
                .customer(customer)
                .expectedOrigin(expectedOrigin)
                .expectedDestination(expectedDestination)
                .taxiType(taxiType)
                .build();

        when(callInfoRepository.save(any(CallInfo.class))).thenReturn(callInfo);

        // When
        CallInfo savedCallInfo = callSaveService.saveCallInfo(callInfo);

        // Then
        assertNotNull(savedCallInfo);
        assertEquals(expectedOrigin, savedCallInfo.getExpectedOrigin());
        assertEquals(expectedDestination, savedCallInfo.getExpectedDestination());
        assertEquals(taxiType, savedCallInfo.getTaxiType());
        verify(callInfoRepository, times(1)).save(callInfo);
    }

    @Test
    @DisplayName("TC02 호출 등록 실패 시 예외 처리 테스트")
    void TC02testSaveCallInfo_Failure() {
        // Given
        Customer customer = Customer.builder()
                .loginId("testuser")
                .password("securepassword")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();

        Location expectedOrigin = new Location(37.5665, 126.9780);
        Location expectedDestination = new Location(37.5651, 126.9895);
        TaxiType taxiType = TaxiType.NORMAL;

        CallInfo callInfo = CallInfo.builder()
                .customer(customer)
                .expectedOrigin(expectedOrigin)
                .expectedDestination(expectedDestination)
                .taxiType(taxiType)
                .build();

        // When: save() 메서드가 RuntimeException을 던지도록 모킹
        when(callInfoRepository.save(any(CallInfo.class)))
                .thenThrow(new RuntimeException("DB 저장 실패"));

        // Then: saveCallInfo() 호출 시 RuntimeException이 발생하는지 검증
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            callSaveService.saveCallInfo(callInfo);
        });

        assertEquals("DB 저장 실패", exception.getMessage());
        verify(callInfoRepository, times(1)).save(callInfo);
    }

    @Test
    @DisplayName("TC03 고객 정보 누락 시 예외 발생")
    void TC03_testSaveCallInfo_NullCustomer_ThrowsException() {
        // Given
        Location expectedOrigin = new Location(37.5665, 126.9780);
        Location expectedDestination = new Location(37.5651, 126.9895);

        CallInfo callInfo = CallInfo.builder()
                .customer(null)  // 고객 정보 누락
                .expectedOrigin(expectedOrigin)
                .expectedDestination(expectedDestination)
                .taxiType(TaxiType.NORMAL)
                .build();

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            callSaveService.saveCallInfo(callInfo);
        });
        assertEquals("고객 정보는 필수입니다.", ex.getMessage());

        verify(callInfoRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC04 출발 위치 정보 누락 시 예외 발생")
    void TC04_testSaveCallInfo_NullExpectedOrigin_ThrowsException() {
        // Given
        Customer customer = Customer.builder()
                .loginId("testuser")
                .password("securepassword")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();

        Location expectedDestination = new Location(37.5651, 126.9895);

        CallInfo callInfo = CallInfo.builder()
                .customer(customer)
                .expectedOrigin(null)  // 출발 위치 누락
                .expectedDestination(expectedDestination)
                .taxiType(TaxiType.NORMAL)
                .build();

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            callSaveService.saveCallInfo(callInfo);
        });
        assertEquals("출발 위치 정보는 필수입니다.", ex.getMessage());

        verify(callInfoRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC05 도착 위치 정보 누락 시 예외 발생")
    void TC05_testSaveCallInfo_NullExpectedDestination_ThrowsException() {
        // Given
        Customer customer = Customer.builder()
                .loginId("testuser")
                .password("securepassword")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();

        Location expectedOrigin = new Location(37.5665, 126.9780);

        CallInfo callInfo = CallInfo.builder()
                .customer(customer)
                .expectedOrigin(expectedOrigin)
                .expectedDestination(null)  // 도착 위치 누락
                .taxiType(TaxiType.NORMAL)
                .build();

        // When & Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            callSaveService.saveCallInfo(callInfo);
        });
        assertEquals("도착 위치 정보는 필수입니다.", ex.getMessage());

        verify(callInfoRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC06 출발 위치 정보가 대한민국 범위 밖일 경우 예외 발생")
    void TC06_testSaveCallInfo_InvalidExpectedOrigin_ThrowsException() {
        Customer customer = Customer.builder()
                .loginId("testuser")
                .password("securepassword")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();

        // 대한민국 위도 범위 밖 (32.0)
        Location invalidOrigin = new Location(32.0, 126.9780);
        Location expectedDestination = new Location(37.5651, 126.9895);

        CallInfo callInfo = CallInfo.builder()
                .customer(customer)
                .expectedOrigin(invalidOrigin)
                .expectedDestination(expectedDestination)
                .taxiType(TaxiType.NORMAL)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            callSaveService.saveCallInfo(callInfo);
        });

        assertEquals("출발 위치 정보가 대한민국 범위를 벗어났습니다.", ex.getMessage());
        verify(callInfoRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC07 도착 위치 정보가 대한민국 범위 밖일 경우 예외 발생")
    void TC07_testSaveCallInfo_InvalidExpectedDestination_ThrowsException() {
        Customer customer = Customer.builder()
                .loginId("testuser")
                .password("securepassword")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();

        Location expectedOrigin = new Location(37.5665, 126.9780);
        // 대한민국 경도 범위 밖 (132.0)
        Location invalidDestination = new Location(37.5651, 132.0);

        CallInfo callInfo = CallInfo.builder()
                .customer(customer)
                .expectedOrigin(expectedOrigin)
                .expectedDestination(invalidDestination)
                .taxiType(TaxiType.NORMAL)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            callSaveService.saveCallInfo(callInfo);
        });

        assertEquals("도착 위치 정보가 대한민국 범위를 벗어났습니다.", ex.getMessage());
        verify(callInfoRepository, never()).save(any());
    }

}
