package com.msik404.karmaappgateway.grpc.client.mapper;

import com.msik404.karmaappgateway.post.dto.PostDto;
import com.msik404.karmaappgateway.post.dto.PostWithImageDataDto;
import com.msik404.karmaappposts.grpc.Post;
import com.msik404.karmaappposts.grpc.PostWithImageData;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;

public class PostDtoMapper {

    @NonNull
    public static PostDto map(@NonNull Post post, @NonNull String username) {

        return new PostDto(
                new ObjectId(post.getPostId().getHexString()),
                new ObjectId(post.getUserId().getHexString()),
                username,
                post.hasHeadline() ? post.getHeadline() : null,
                post.hasText() ? post.getText() : null,
                post.getKarmaScore(),
                post.getVisibility()
        );
    }

    @NonNull
    public static PostWithImageDataDto map(@NonNull PostWithImageData post, @NonNull String username) {

        return new PostWithImageDataDto(
                map(post.getPost(), username),
                post.hasImageData() ? post.getImageData().toByteArray() : null
        );
    }
}
