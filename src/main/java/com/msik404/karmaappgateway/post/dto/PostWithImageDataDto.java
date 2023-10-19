package com.msik404.karmaappgateway.post.dto;

import lombok.NonNull;

public record PostWithImageDataDto(@NonNull PostDto postDto, byte[] imageData) {

}
