package goorm.humandelivery.shared.dto.response;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.Getter;

import java.time.Instant;

@Getter
public class TokenInfoResponse {
    private String loginId;
    private Instant issuedAt;
    private Instant expiresAt;
    private boolean expired;

    private TokenInfoResponse(String loginId, Instant issuedAt, Instant expiresAt, boolean expired) {
        this.loginId = loginId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.expired = expired;
    }

    public static TokenInfoResponse from(Claims claims) {

        String loginId = claims.getSubject();
        Instant issuedAt = Instant.ofEpochSecond(((Number) claims.get("iat")).longValue());
        Instant expiresAt = Instant.ofEpochSecond(((Number) claims.get("exp")).longValue());

        return new TokenInfoResponse(
                loginId,
                issuedAt,
                expiresAt,
                false
        );
    }

    public static TokenInfoResponse from(ExpiredJwtException e) {

        Claims claims = e.getClaims();
        String loginId = claims.getSubject();
        Instant issuedAt = Instant.ofEpochSecond(((Number) claims.get("iat")).longValue());
        Instant expiresAt = Instant.ofEpochSecond(((Number) claims.get("exp")).longValue());

        return new TokenInfoResponse(
                loginId,
                issuedAt,
                expiresAt,
                true
        );
    }
}
