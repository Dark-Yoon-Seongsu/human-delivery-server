package goorm.humandelivery.common.application.port.out;

import goorm.humandelivery.domain.model.response.TokenInfoResponse;
import org.springframework.security.core.Authentication;

public interface JwtTokenProviderPort {

    String generateToken(String loginId);

    boolean validateToken(String token);

    TokenInfoResponse extractTokenInfo(String token);

    Authentication getAuthentication(String token);
}
