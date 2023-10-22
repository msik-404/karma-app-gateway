package com.msik404.karmaappgateway.user.dto;

import jakarta.validation.constraints.Email;
import org.springframework.lang.Nullable;

public record UserUpdateRequestWithUserPrivilege(
        @Nullable String firstName,
        @Nullable String lastName,
        @Nullable String username,
        @Email @Nullable String email,
        @Nullable String password) {
}
