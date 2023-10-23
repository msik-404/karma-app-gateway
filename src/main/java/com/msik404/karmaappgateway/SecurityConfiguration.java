package com.msik404.karmaappgateway;

import com.msik404.karmaappgateway.auth.jwt.JwtAuthenticationFilter;
import com.msik404.karmaappgateway.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DelegatedAuthenticationEntryPoint entryPoint;

    @Bean
    public static RoleHierarchy roleHierarchy() {

        var hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_MOD\nROLE_MOD > ROLE_USER\n");
        return hierarchy;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        // I disabled many of the filters, because I don't like to have
        // things that I won't use
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .securityContext(AbstractHttpConfigurer::disable) // This is for securityContext persistence
                .anonymous(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)
                .requestCache(RequestCacheConfigurer::disable)

                .exceptionHandling(handler -> handler.authenticationEntryPoint(entryPoint))

                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/admin/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers("/mod/**").hasAnyAuthority(Role.ADMIN.name(), Role.MOD.name())
                        .requestMatchers("/user/**").hasAnyAuthority(Role.ADMIN.name(), Role.MOD.name(), Role.USER.name())
                        .requestMatchers("/register", "/login", "/static/**", "guest/**").permitAll()
                        .anyRequest().authenticated())

                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, AuthorizationFilter.class)
                .build();
    }

}
