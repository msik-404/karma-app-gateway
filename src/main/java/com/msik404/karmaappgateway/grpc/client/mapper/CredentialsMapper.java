package com.msik404.karmaappgateway.grpc.client.mapper;

import com.msik404.karmaappgateway.grpc.client.exception.UnsupportedRoleException;
import com.msik404.karmaappgateway.user.UserDetailsImpl;
import com.msik404.karmaappusers.grpc.CredentialsResponse;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;

public class CredentialsMapper {

    @NonNull
    public static UserDetailsImpl map(@NonNull CredentialsResponse response) throws UnsupportedRoleException {

        return new UserDetailsImpl(
                new ObjectId(response.getUserId().getHexString()),
                response.getPassword(),
                RoleMapper.map(response.getRole())
        );
    }

}
