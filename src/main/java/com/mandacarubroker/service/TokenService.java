package com.mandacarubroker.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mandacarubroker.domain.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    @Value("${spring.security.secret}")
    private String SECRET_JWT;

    public String generateToken(User user){
        Algorithm algorithm = Algorithm.HMAC256(SECRET_JWT);
        return JWT.create()
                .withIssuer("mandacarubroker")
                .withExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .withSubject(user.getUsername())
                .sign(algorithm);
    }

    public String validateToken(String token){
        Algorithm algorithm = Algorithm.HMAC256(SECRET_JWT);
        return JWT.require(algorithm)
                .withIssuer("mandacarubroker").build()
                .verify(token).getSubject();
    }

}
