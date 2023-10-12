package com.msik404.karmaappgateway.dto;

import org.springframework.lang.NonNull;

public record ScrollPosition(@NonNull String postIdHexString, long karmaScore) {
}