package com.msik404.karmaappgateway;

import com.msik404.karmaappgateway.converter.ObjectIdConverter;
import com.msik404.karmaappgateway.user.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class KarmaAppGatewayConfiguration implements WebMvcConfigurer {


    @NonNull
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @NonNull
    @Bean
    public AuthenticationProvider authenticationProvider(@NonNull UserDetailsServiceImpl userDetailsService) {

        var authProvider = new DaoAuthenticationProvider(bCryptPasswordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @NonNull
    @Bean
    public AuthenticationManager authenticationManager(
            @NonNull AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(new ObjectIdConverter());
    }

}
