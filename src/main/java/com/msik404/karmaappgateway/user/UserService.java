package com.msik404.karmaappgateway.user;

import com.msik404.karmaappgateway.grpc.client.GrpcService;
import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaappgateway.user.exception.DuplicateEmailException;
import com.msik404.karmaappgateway.user.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappgateway.user.exception.DuplicateUsernameException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final GrpcService grpcService;

    public void updateWithUserPrivilege(
            @NonNull UserUpdateRequestWithUserPrivilege request
    ) throws DuplicateUsernameException, DuplicateEmailException, DuplicateUnexpectedFieldException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var clientId = (ObjectId) authentication.getPrincipal();

        grpcService.updateUserWithUserPrivilege(clientId, request);
    }

    public void updateWithAdminPrivilege(
            @NonNull ObjectId userId,
            @NonNull UserUpdateRequestWithAdminPrivilege request
    ) throws DuplicateUsernameException, DuplicateEmailException, DuplicateUnexpectedFieldException {

        grpcService.updateUserWithAdminPrivilege(userId, request);
    }

}
