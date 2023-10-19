package com.msik404.karmaappgateway.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

public record RegisterRequest(@NotNull String username, @NotNull @Email String email, @NotNull String password,
                              @Nullable String firstName, @Nullable String lastName) {

}
