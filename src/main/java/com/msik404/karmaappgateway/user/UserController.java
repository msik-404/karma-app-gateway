package com.msik404.karmaappgateway.user;

import com.msik404.karmaappgateway.docs.KarmaAppEndpointDocs;
import com.msik404.karmaappgateway.docs.SwaggerConfiguration;
import com.msik404.karmaappgateway.docs.UserUpdateRequestWithAdminPrivilegeDoc;
import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaappgateway.user.exception.DuplicateEmailException;
import com.msik404.karmaappgateway.user.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappgateway.user.exception.DuplicateUsernameException;
import com.msik404.karmaappgateway.user.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = SwaggerConfiguration.AUTH)
public class UserController {

    private final UserService userService;


    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_UPDATE_WITH_USER_PRIVILEGE,
            description = KarmaAppEndpointDocs.OP_DESC_UPDATE_WITH_USER_PRIVILEGE
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_ACCOUNT_UPDATE
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = UserNotFoundException.ERROR_MESSAGE + """
                             This exception might be thrown is user's account gets deleted after start of update but
                            before end of update. This practically cannot happen, because there is no functionality for
                            deleting users.
                            """,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
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
    @PutMapping("user/users")
    public ResponseEntity<UserUpdateRequestWithUserPrivilege> updateWithUserPrivilege(

            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_USER_UPDATE_REQUEST_WITH_USER_PRIVILEGE)
            @Valid @RequestBody UserUpdateRequestWithUserPrivilege request
    ) throws UserNotFoundException, DuplicateUsernameException, DuplicateEmailException,
            DuplicateUnexpectedFieldException {

        userService.updateWithUserPrivilege(request);

        return ResponseEntity.ok(null);
    }


    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_UPDATE_WITH_ADMIN_PRIVILEGE,
            description = KarmaAppEndpointDocs.OP_DESC_UPDATE_WITH_ADMIN_PRIVILEGE
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_ACCOUNT_UPDATE
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = UserNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
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
    @PutMapping("admin/users/{userId}")
    public ResponseEntity<UserUpdateRequestWithAdminPrivilege> updateWithAdminPrivilege(

            @Parameter(description = KarmaAppEndpointDocs.PATH_DESC_USER_ID)
            @PathVariable ObjectId userId,

            @Parameter(
                    description = KarmaAppEndpointDocs.PARAM_DESC_USER_UPDATE_REQUEST_WITH_ADMIN_PRIVILEGE,
                    schema = @Schema(
                            name = "UserUpdateRequestWithAdminPrivilege",
                            implementation = UserUpdateRequestWithAdminPrivilegeDoc.class
                    )
            )
            @Valid @RequestBody UserUpdateRequestWithAdminPrivilege request
    ) throws UserNotFoundException, DuplicateUsernameException, DuplicateEmailException,
            DuplicateUnexpectedFieldException {

        userService.updateWithAdminPrivilege(userId, request);

        return ResponseEntity.ok(null);
    }

}
