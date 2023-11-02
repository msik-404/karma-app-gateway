package com.msik404.karmaappgateway.user;

import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaappgateway.user.exception.DuplicateEmailException;
import com.msik404.karmaappgateway.user.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappgateway.user.exception.DuplicateUsernameException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("user/users")
    public ResponseEntity<UserUpdateRequestWithUserPrivilege> updateWithUserPrivilege(
            @Valid @RequestBody UserUpdateRequestWithUserPrivilege request
    ) throws DuplicateUsernameException, DuplicateEmailException, DuplicateUnexpectedFieldException {

        userService.updateWithUserPrivilege(request);

        return ResponseEntity.ok(null);
    }

    @PutMapping("admin/users/{userId}")
    public ResponseEntity<UserUpdateRequestWithAdminPrivilege> updateWithAdminPrivilege(
            @PathVariable ObjectId userId,
            @Valid @RequestBody UserUpdateRequestWithAdminPrivilege request
    ) throws DuplicateUsernameException, DuplicateEmailException, DuplicateUnexpectedFieldException {

        userService.updateWithAdminPrivilege(userId, request);

        return ResponseEntity.ok(null);
    }

}
