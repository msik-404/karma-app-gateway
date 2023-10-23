package com.msik404.karmaappgateway.grpc.client.mapper;

import java.util.Optional;

import com.msik404.karmaappgateway.grpc.client.exception.UnsupportedRoleException;
import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaappusers.grpc.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RequiredArgsConstructor
public class UserUpdateMapper {


    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @NonNull
    private Optional<UpdateUserRequest.Builder> mapImpl(
            @NonNull ObjectId userId,
            @NonNull UserUpdateRequestWithUserPrivilege request
    ) {

        var builder = UpdateUserRequest.newBuilder();
        builder.setUserId(MongoObjectIdMapper.mapToUsersMongoObjectId(userId));

        boolean somethingWasSet = false;

        if (request.firstName() != null) {
            somethingWasSet = true;
            builder.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            somethingWasSet = true;
            builder.setLastName(request.lastName());
        }
        if (request.username() != null) {
            somethingWasSet = true;
            builder.setUsername(request.username());
        }
        if (request.email() != null) {
            somethingWasSet = true;
            builder.setEmail(request.email());
        }
        if (request.password() != null) {
            somethingWasSet = true;
            builder.setPassword(bCryptPasswordEncoder.encode(request.password()));
        }

        if (somethingWasSet) {
            return Optional.of(builder);
        }
        return Optional.empty();
    }

    @NonNull
    public Optional<UpdateUserRequest> map(
            @NonNull ObjectId userId,
            @NonNull UserUpdateRequestWithUserPrivilege request
    ) {
        return mapImpl(userId, request).map(UpdateUserRequest.Builder::build);
    }

    @NonNull
    public Optional<UpdateUserRequest> map(
            @NonNull ObjectId userId,
            @NonNull UserUpdateRequestWithAdminPrivilege request
    ) throws UnsupportedRoleException {

        boolean somethingWasSet = false;

        UpdateUserRequest.Builder builder;
        if (request.userUpdateByUser() != null) {
            Optional<UpdateUserRequest.Builder> optionalBuilder = mapImpl(userId, request.userUpdateByUser());
            if (optionalBuilder.isPresent()) {
                somethingWasSet = true;
                builder = optionalBuilder.get();
            } else {
                builder = UpdateUserRequest.newBuilder();
            }
        } else {
            builder = UpdateUserRequest.newBuilder();
        }

        if (request.role() != null) {
            builder.setRole(RoleMapper.map(request.role()));
        }

        if (somethingWasSet) {
            return Optional.of(builder.build());
        }
        return Optional.empty();
    }
}
