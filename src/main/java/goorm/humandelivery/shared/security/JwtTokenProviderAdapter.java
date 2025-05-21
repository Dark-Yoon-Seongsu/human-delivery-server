package goorm.humandelivery.shared.security;

import goorm.humandelivery.shared.dto.response.TokenInfoResponse;
import goorm.humandelivery.shared.security.port.out.JwtTokenProviderPort;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProviderAdapter implements JwtTokenProviderPort {
    // 헤더, 페이로드, 서명
    // 페이로드 + secret -> HSMAC -> 서명 이 나옵니다.
    // 페이로드에는 클레임 ( 정보 )
    // 헤더에는 HSMAC 서명을 뭘로 햇는지 알고리즘이 나와잇어요.

    // 해당 클래스가 빈에 등록된 후 key 를 초기화합니다.
    // 현재 secret 을 외부 환경파일로부터 주입받고 있기 때문에, 빈 등록 후 해당 필드를 초기화 하는 것이 안전하다고 합니다.

    @Value("${jwt.secret.access}")
    private String secret;

    @Value("${jwt.secret.accessTokenValidTime}")
    private Duration accessTokenValidTime;

    private Key key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 토큰 생성 로직입니다.
    @Override
    public String generateToken(String loginId) {

        // 현재시간
        Instant now = Instant.now();

        /**
         * 클레임을 따로 설정할 필요가 없을 것 같아서 일단 subJect()에 아이디만 담았습니다.
         * 추후 더 필요한 정보가 있다면 추가하겠습니다.
         */
        return Jwts.builder()
                .setSubject(loginId) // 클레임
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessTokenValidTime)))
                .signWith(key, SignatureAlgorithm.HS256) // 키와 페이로드(클레임을) HS256 알고리즘 인풋으로 -> 서명 생멍
                .compact();
    }

    // 토큰 검증
    // WebSocketConfig 에서 사용됩니다.
    @Override
    public boolean validateToken(String token) {
        try {
            // 사용자 서명과 == 사용자 페이로드 + 내 비밀키로 -> 서명 생성  검증
            // parseClaimsJws 안에 서명 검증, 페이로드 파싱, 만료 검사 등등 모두 포함되어 있음.
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty.", e);
        }
        return false;
    }

    // 토큰 정보 반환 로직에 사용됩니다.
    @Override
    public TokenInfoResponse extractTokenInfo(String token) {
        try {
            Claims claims = parseClaims(token);
            return TokenInfoResponse.from(claims);

        } catch (ExpiredJwtException e) {
            // 만료된 토큰이지만 claims 는 꺼낼 수 있음
            return TokenInfoResponse.from(e);
        }
    }

    // STOMP 헤더에서 토큰정보 가져오고 -> 시큐리티 컨텍스트에 넣는 코드
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token); // JWT 파싱
        String loginId = claims.getSubject();

        // 필요하다면 권한도 넣기.. 구분이 필요하다면 나중에 변경하는걸로
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        // Authentication 객체 생성. -> 이걸 시큐리티 컨텍스트에 넣는 방식 것이 기존 우리가 하던 세션 방식의 로그인 프로세스.
        // 하지만 웹소켓에서는 필요없는 과정
        // UsernamePasswordAuthenticationToken -> SecurityContextholder -> authenticate 로 저장이 됩니다.
        return new UsernamePasswordAuthenticationToken(loginId, null, authorities);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
