package com.msik404.karmaappgateway.user;

import com.msik404.karmaappgateway.grpc.client.GrpcService;
import com.msik404.karmaappgateway.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final GrpcService service;

    @Override
    public UserDetails loadUserByUsername(
            @NonNull String email
    ) throws UserNotFoundException {

        return service.findUserDetails(email);
    }

}
