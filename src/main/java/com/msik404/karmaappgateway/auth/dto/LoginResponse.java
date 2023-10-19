package com.msik404.karmaappgateway.auth.dto;

import org.springframework.lang.NonNull;

public record LoginResponse(@NonNull String jwt) {
}

