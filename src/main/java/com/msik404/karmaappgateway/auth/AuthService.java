package com.msik404.karmaappgateway.auth;

import com.msik404.karmaappgateway.auth.dto.LoginRequest;
import com.msik404.karmaappgateway.auth.dto.LoginResponse;
import com.msik404.karmaappgateway.auth.dto.RegisterRequest;
import com.msik404.karmaappgateway.auth.jwt.JwtService;
import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import com.msik404.karmaappgateway.grpc.client.GrpcService;
import com.msik404.karmaappgateway.grpc.client.exception.InternalRestException;
import com.msik404.karmaappgateway.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final GrpcService grpcService;

    public void register(
            @NonNull RegisterRequest request
    ) throws RestFromGrpcException, InternalRestException {

        grpcService.registerUser(request);
    }

    public LoginResponse login(@NonNull LoginRequest request) throws AuthenticationException {

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        // This authentication comes from UserDetailsServiceImpl and principal object is UserDetailsImpl.
        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ObjectId clientId = userDetails.userId();

        return new LoginResponse(jwtService.generateJwt(clientId, null));
    }

}
