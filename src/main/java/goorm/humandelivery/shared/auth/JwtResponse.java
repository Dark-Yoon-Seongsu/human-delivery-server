package goorm.humandelivery.shared.auth;

import lombok.Getter;

@Getter
public class JwtResponse {

    private final String token;

    public JwtResponse(String token) {
        this.token = token;
    }
}


