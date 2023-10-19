package com.msik404.karmaappgateway.post.dto;

import jakarta.validation.constraints.NotNull;

public record PostCreationRequest(@NotNull String headline, @NotNull String text) {
}
