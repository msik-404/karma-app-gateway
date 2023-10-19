package com.msik404.karmaappgateway.post.dto;

import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;

public record ScrollPosition(@NonNull ObjectId postId, long karmaScore) {
}