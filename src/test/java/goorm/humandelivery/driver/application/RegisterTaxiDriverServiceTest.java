package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.domain.*;
import goorm.humandelivery.global.exception.DuplicateLoginIdException;
import goorm.humandelivery.driver.application.port.out.SaveTaxiDriverPort;
import goorm.humandelivery.driver.application.port.out.SaveTaxiPort;
import goorm.humandelivery.driver.dto.request.RegisterTaxiDriverRequest;
import goorm.humandelivery.driver.dto.request.RegisterTaxiRequest;
import goorm.humandelivery.driver.dto.response.RegisterTaxiDriverResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//./gradlew test --tests "goorm.humandelivery.driver.application.RegisterTaxiDriverServiceTest"
@ExtendWith(MockitoExtension.class)
class RegisterTaxiDriverServiceTest {

    @Mock
    private SaveTaxiDriverPort saveTaxiDriverPort;

    @Mock
    private SaveTaxiPort saveTaxiPort;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterTaxiDriverService registerTaxiDriverService;

    private RegisterTaxiDriverRequest validRequest;

    @BeforeEach
    void setUp() {
        RegisterTaxiRequest taxiRequest = RegisterTaxiRequest.builder()
                .model("Hyundai Sonata")
                .plateNumber("12가3456")
                .taxiType(String.valueOf(TaxiType.NORMAL))
                .fuelType(String.valueOf(FuelType.GASOLINE))
                .build();

        validRequest = RegisterTaxiDriverRequest.builder()
                .loginId("driver123")
                .password("rawPassword")
                .name("홍길동")
                .licenseCode("LIC-1234")
                .phoneNumber("01012345678")
                .taxi(taxiRequest)
                .build();

    }

    @Test
    @DisplayName("중복된 로그인 ID일 경우 DuplicateLoginIdException을 발생시킨다")
    void register_throwsException_whenLoginIdIsDuplicate() {
        // given
        when(saveTaxiDriverPort.existsByLoginId("driver123")).thenReturn(true);

        // when & then
        assertThrows(DuplicateLoginIdException.class, () ->
                registerTaxiDriverService.register(validRequest));
    }

    @Test
    @DisplayName("정상적인 요청일 경우 기사와 택시 정보를 저장하고 응답을 반환한다")
    void register_savesDriverAndTaxi_whenValidRequest() {
        // given
        when(saveTaxiDriverPort.existsByLoginId("driver123")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        Taxi savedTaxi = Taxi.builder()
                .model("Hyundai Sonata")
                .plateNumber("12가3456")
                .taxiType(TaxiType.NORMAL)
                .fuelType(FuelType.GASOLINE)
                .build();
        when(saveTaxiPort.save(any())).thenReturn(savedTaxi);

        TaxiDriver savedDriver = TaxiDriver.builder()
                .loginId("driver123")
                .password("encodedPassword")
                .taxi(savedTaxi)
                .name("홍길동")
                .licenseCode("LIC-1234")
                .phoneNumber("01012345678")
                .status(TaxiDriverStatus.OFF_DUTY)
                .build();
        when(saveTaxiDriverPort.save(any())).thenReturn(savedDriver);

        // when
        RegisterTaxiDriverResponse response = registerTaxiDriverService.register(validRequest);

        // then
        assertEquals("driver123", response.getLoginId());
        assertEquals("홍길동", response.getName());
        verify(saveTaxiDriverPort).save(any());
        verify(saveTaxiPort).save(any());
    }

    @Test
    @DisplayName("비밀번호는 암호화된 상태로 저장되어야 한다")
    void register_encodesPassword_beforeSavingDriver() {

        // given
        when(saveTaxiDriverPort.existsByLoginId(any())).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        Taxi mockTaxi = Taxi.builder().build();
        when(saveTaxiPort.save(any())).thenReturn(mockTaxi);

        when(saveTaxiDriverPort.save(any())).thenAnswer(invocation -> {
            TaxiDriver driver = invocation.getArgument(0);
            assertEquals("encodedPassword", driver.getPassword());
            return driver.toBuilder().build();
        });

        // when
        registerTaxiDriverService.register(validRequest);

        // then
        verify(passwordEncoder).encode("rawPassword");
    }

    @Test
    @DisplayName("택시 저장 중 예외가 발생하면 예외가 전파된다")
    void register_throwsException_whenTaxiSaveFails() {
        // given
        when(saveTaxiDriverPort.existsByLoginId(any())).thenReturn(false);
        when(saveTaxiPort.save(any())).thenThrow(new RuntimeException("DB error"));

        // when & then
        assertThrows(RuntimeException.class, () -> registerTaxiDriverService.register(validRequest));
    }

    @Test
    @DisplayName("기사 저장 중 예외가 발생하면 예외가 전파된다")
    void register_throwsException_whenDriverSaveFails() {
        // given
        when(saveTaxiDriverPort.existsByLoginId(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        Taxi mockTaxi = Taxi.builder().build();
        when(saveTaxiPort.save(any())).thenReturn(mockTaxi);
        when(saveTaxiDriverPort.save(any())).thenThrow(new RuntimeException("Driver DB error"));

        // when & then
        assertThrows(RuntimeException.class, () -> registerTaxiDriverService.register(validRequest));
    }
}
