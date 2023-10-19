package com.msik404.karmaappgateway.auth.jwt;

import java.io.IOException;
import java.util.List;

import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import com.msik404.karmaappgateway.grpc.client.GrpcService;
import com.msik404.karmaappgateway.grpc.client.exception.InternalRestException;
import com.msik404.karmaappgateway.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final GrpcService grpcService;

    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain
    ) throws ServletException, IOException, JwtException, IllegalArgumentException, RestFromGrpcException,
            InternalRestException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authHeader.substring(TOKEN_PREFIX.length());
        final Claims claims = jwtService.extractAllClaims(jwt);
        // 1. Get subject from token claims, null if something is wrong
        // 2. Checks whether user is not already authenticated,
        // this is useful when there are many authentication methods.
        if (claims.getSubject() != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // parse userId Long type represented as string to Long type
            final ObjectId userId = new ObjectId(claims.getSubject());
            final Role role = grpcService.findUserRole(userId);
            final var authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(new SimpleGrantedAuthority(role.name()))
            );
            // Adds interesting data like ip address and session id
            authentication.setDetails(new WebAuthenticationDetails(request));
            // Docs state that this is required for thread safety:
            // https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html
            final SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }
        filterChain.doFilter(request, response);
    }

}
