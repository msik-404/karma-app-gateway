package com.msik404.karmaappgateway.post.dto;

import lombok.NonNull;
import org.springframework.lang.Nullable;

public record PostWithImageDataDto(@NonNull PostDto postDto, @Nullable byte[] imageData) {
}
