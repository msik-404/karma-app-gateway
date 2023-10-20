package com.msik404.karmaappgateway.auth;

import com.msik404.karmaappgateway.auth.dto.LoginRequest;
import com.msik404.karmaappgateway.auth.dto.LoginResponse;
import com.msik404.karmaappgateway.auth.dto.RegisterRequest;
import com.msik404.karmaappgateway.user.exception.DuplicateEmailException;
import com.msik404.karmaappgateway.user.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappgateway.user.exception.DuplicateUsernameException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request
    ) throws DuplicateUsernameException, DuplicateEmailException, DuplicateUnexpectedFieldException {

        authenticationService.register(request);

        return ResponseEntity.ok(null);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request)
            throws AuthenticationException {

        return ResponseEntity.ok(authenticationService.login(request));
    }

}
