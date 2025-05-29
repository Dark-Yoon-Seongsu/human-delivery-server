package goorm.humandelivery.driver.application;

import goorm.humandelivery.driver.application.port.out.LoadTaxiDriverPort;
import goorm.humandelivery.driver.domain.TaxiDriver;
import goorm.humandelivery.driver.dto.request.LoginTaxiDriverRequest;
import goorm.humandelivery.global.exception.IncorrectPasswordException;
import goorm.humandelivery.global.exception.JwtTokenGenerationException;
import goorm.humandelivery.global.exception.DriverEntityNotFoundException;
import goorm.humandelivery.shared.dto.response.JwtResponse;
import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

//./gradlew test --tests "goorm.humandelivery.driver.application.LoginTaxiDriverServiceTest"
@ExtendWith(MockitoExtension.class)
class LoginTaxiDriverServiceTest {

    @Mock
    LoadTaxiDriverPort loadTaxiDriverPort = mock(LoadTaxiDriverPort.class);
    @Mock
    JwtTokenProviderPort jwtTokenProviderPort = mock(JwtTokenProviderPort.class);
    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);

    @InjectMocks
    LoginTaxiDriverService loginTaxiDriverService;

    @Test
    @DisplayName("로그인 성공")
    void login_success_returnsJwtResponse() {
        // 요청에 맞춘 loginId와 password
        String loginId = "driver1";
        String rawPassword = "password";

        LoginTaxiDriverRequest validRequest = LoginTaxiDriverRequest.builder()
                .loginId(loginId)
                .password(rawPassword)
                .build();

        TaxiDriver taxiDriver = TaxiDriver.builder()
                .loginId(loginId)
                .password("encodedPassword")  // DB에 저장된 암호화된 비밀번호
                .build();

        // given
        when(loadTaxiDriverPort.findByLoginId(loginId)).thenReturn(Optional.of(taxiDriver));
        when(bCryptPasswordEncoder.matches(rawPassword, taxiDriver.getPassword())).thenReturn(true);
        when(jwtTokenProviderPort.generateToken(loginId)).thenReturn("dummy-jwt-token");

        // when
        JwtResponse response = loginTaxiDriverService.login(validRequest);

        // then
        assertNotNull(response);
        assertEquals("dummy-jwt-token", response.getToken());
        verify(loadTaxiDriverPort).findByLoginId(loginId);
        verify(bCryptPasswordEncoder).matches(rawPassword, taxiDriver.getPassword());
        verify(jwtTokenProviderPort).generateToken(loginId);
    }


    @Test
    @DisplayName("로그인 ID가 null이면 IllegalArgumentException 발생")
    void login_throwsException_whenLoginIdIsNull() {
        LoginTaxiDriverRequest request = LoginTaxiDriverRequest.builder().loginId(null).password("password").build();
        assertThrows(IllegalArgumentException.class, () -> loginTaxiDriverService.login(request));
    }

    @Test
    @DisplayName("비밀번호가 null이면 IllegalArgumentException 발생")
    void login_throwsException_whenPasswordIsNull() {
        LoginTaxiDriverRequest request = LoginTaxiDriverRequest.builder().loginId("driver1").password(null).build();
        assertThrows(IllegalArgumentException.class, () -> loginTaxiDriverService.login(request));
    }

    @Test
    @DisplayName("존재하지 않는 ID로 로그인하면 TaxiDriverEntityNotFoundException 발생")
    void login_throwsException_whenTaxiDriverNotFound() {
        given(loadTaxiDriverPort.findByLoginId("driver1")).willReturn(Optional.empty());
        LoginTaxiDriverRequest request = LoginTaxiDriverRequest.builder().loginId("driver1").password("password").build();

        assertThrows(DriverEntityNotFoundException.class, () -> loginTaxiDriverService.login(request));
    }

    @Test
    @DisplayName("비밀번호 불일치 시 IncorrectPasswordException 발생")
    void login_throwsException_whenPasswordDoesNotMatch() {
        TaxiDriver found = TaxiDriver.builder()
                .loginId("driver1")
                .password("hashedPassword")
                .build();
        given(loadTaxiDriverPort.findByLoginId("driver1")).willReturn(Optional.of(found));
        given(bCryptPasswordEncoder.matches("wrongPassword", "hashedPassword")).willReturn(false);

        LoginTaxiDriverRequest request = LoginTaxiDriverRequest.builder().loginId("driver1").password("wrongPassword").build();


        assertThrows(IncorrectPasswordException.class, () -> loginTaxiDriverService.login(request));
    }

    @Test
    @DisplayName("JWT 발급 중 예외가 발생하면 JwtTokenGenerationException 발생")
    void login_throwsException_whenJwtTokenGenerationFails() {
        TaxiDriver found = TaxiDriver.builder()
                .loginId("driver1")
                .password("hashedPassword")
                .build();
        given(loadTaxiDriverPort.findByLoginId("driver1")).willReturn(Optional.of(found));
        given(bCryptPasswordEncoder.matches("password", "hashedPassword")).willReturn(true);
        given(jwtTokenProviderPort.generateToken("driver1")).willThrow(new RuntimeException("JWT 실패"));

        LoginTaxiDriverRequest request = LoginTaxiDriverRequest.builder().loginId("driver1").password("password").build();

        assertThrows(JwtTokenGenerationException.class, () -> loginTaxiDriverService.login(request));
    }
}
