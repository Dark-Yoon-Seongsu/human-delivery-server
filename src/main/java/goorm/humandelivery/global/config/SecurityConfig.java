package goorm.humandelivery.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 서블릿 검증
    // 서블릿 -> HTTP 요청 받아서 비즈니스 로직 처리하고 응답보내주는 자바 클래스.
    // 디스패처 서블릿
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/taxi-driver/**",
                                "/api/v1/taxi-driver/auth-tokens",
                                "/api/v1/taxi-driver/token-info",
                                "/api/v1/customer",
                                "/api/v1/customer/auth-tokens",
                                "/ws/**",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

}

