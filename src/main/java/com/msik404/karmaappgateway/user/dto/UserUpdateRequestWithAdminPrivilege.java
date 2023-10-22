package com.msik404.karmaappgateway.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.msik404.karmaappgateway.deserializer.UserUpdateRequestWithAdminPrivilegeDeserializer;
import com.msik404.karmaappgateway.user.Role;
import jakarta.validation.Valid;
import org.springframework.lang.Nullable;

@JsonDeserialize(using = UserUpdateRequestWithAdminPrivilegeDeserializer.class)
public record UserUpdateRequestWithAdminPrivilege(
        @Valid @Nullable UserUpdateRequestWithUserPrivilege userUpdateByUser,
        @Nullable Role role) {
}
