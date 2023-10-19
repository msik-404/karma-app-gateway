package com.msik404.karmaappgateway.grpc.client.mapper;

import com.msik404.karmaappgateway.post.dto.PostRatingResponse;
import com.msik404.karmaappposts.grpc.PostRating;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;

public class PostRatingMapper {

    @NonNull
    public static PostRatingResponse map(@NonNull PostRating postRating) {

        return new PostRatingResponse(
                new ObjectId(postRating.getPostId().getHexString()),
                postRating.hasIsPositive() ? postRating.getIsPositive() : null
        );
    }

}
