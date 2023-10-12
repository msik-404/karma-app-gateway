package com.msik404.karmaappgateway.dto;

import lombok.NonNull;

public record PostWithImageDataDto(@NonNull PostDto postDto, byte[] imageData) {

}
