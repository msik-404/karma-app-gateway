package com.msik404.karmaappgateway.auth.jwt;

import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private static final int TIME_TO_EXPIRE = 1000 * 60 * 60 * 1; // one hour

    /**
     * Generates new JWT.
     * Subject is set to user's Long type identifier which will be transformed to string.
     *
     * @param clientId mongodb unique id value for client
     * @param opt      additional claims which will be added to JWT
     * @return string with JWT
     */
    @NonNull
    public String generateJwt(
            @NonNull ObjectId clientId,
            @Nullable Map<String, Object> opt) {

        long currentTime = System.currentTimeMillis();
        // one hour
        long expirationTime = currentTime + TIME_TO_EXPIRE;

        JwtBuilder builder = Jwts.builder();

        if (opt != null) {
            builder.claims().add(opt);
        }

        return builder
                .subject(clientId.toHexString())
                .issuedAt(new Date(currentTime))
                .expiration(new Date(expirationTime))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    public Claims extractAllClaims(
            @NonNull String jwt
    ) throws JwtException, IllegalArgumentException {

        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}
