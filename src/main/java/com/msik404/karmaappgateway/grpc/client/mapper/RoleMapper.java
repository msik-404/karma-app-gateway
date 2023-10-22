package com.msik404.karmaappgateway.grpc.client.mapper;

import com.msik404.karmaappgateway.grpc.client.exception.UnsupportedRoleException;
import com.msik404.karmaappgateway.user.Role;
import com.msik404.karmaappusers.grpc.UserRole;
import org.springframework.lang.NonNull;

public class RoleMapper {

    @NonNull
    public static Role map(@NonNull UserRole role) throws UnsupportedRoleException {

        return switch (role) {
            case ROLE_USER -> Role.USER;
            case ROLE_MOD -> Role.MOD;
            case ROLE_ADMIN -> Role.ADMIN;
            default -> throw new UnsupportedRoleException();
        };
    }

    @NonNull
    public static UserRole map(@NonNull Role role) {

        return switch (role) {
            case USER -> UserRole.ROLE_USER;
            case MOD -> UserRole.ROLE_MOD;
            case ADMIN -> UserRole.ROLE_ADMIN;
        };
    }

}
