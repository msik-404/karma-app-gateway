package com.msik404.karmaappgateway.auth;

import com.msik404.karmaappgateway.auth.dto.LoginRequest;
import com.msik404.karmaappgateway.auth.dto.LoginResponse;
import com.msik404.karmaappgateway.auth.dto.RegisterRequest;
import com.msik404.karmaappgateway.docs.KarmaAppEndpointDocs;
import com.msik404.karmaappgateway.user.exception.DuplicateEmailException;
import com.msik404.karmaappgateway.user.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappgateway.user.exception.DuplicateUsernameException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authenticationService;

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_REGISTER,
            description = KarmaAppEndpointDocs.OP_DESC_REGISTER
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_REGISTER
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = KarmaAppEndpointDocs.RESP_CONF_NOT_UNIQUE_OBJECT_FIELD,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            )
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_REGISTER_REQUEST)
            @Valid @RequestBody RegisterRequest request
    ) throws DuplicateUsernameException, DuplicateEmailException, DuplicateUnexpectedFieldException {

        authenticationService.register(request);

        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_LOGIN,
            description = KarmaAppEndpointDocs.OP_DESC_LOGIN
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_LOGIN
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = KarmaAppEndpointDocs.RESP_CONF_NOT_UNIQUE_OBJECT_FIELD,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_LOGIN_REQUEST)
            @Valid @RequestBody LoginRequest request
    ) throws AuthenticationException {

        return ResponseEntity.ok(authenticationService.login(request));
    }

}
