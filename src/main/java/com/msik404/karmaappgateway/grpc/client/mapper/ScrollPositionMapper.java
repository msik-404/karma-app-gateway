package com.msik404.karmaappgateway.grpc.client.mapper;

import com.msik404.karmaappgateway.post.dto.ScrollPosition;
import com.msik404.karmaappposts.grpc.MongoObjectId;
import org.springframework.lang.NonNull;

public class ScrollPositionMapper {

    @NonNull
    public static com.msik404.karmaappposts.grpc.ScrollPosition map(@NonNull ScrollPosition scrollPosition) {

        return com.msik404.karmaappposts.grpc.ScrollPosition.newBuilder()
                .setPostId(MongoObjectId.newBuilder().setHexString(scrollPosition.postId().toHexString()).build())
                .setKarmaScore(scrollPosition.karmaScore())
                .build();
    }

}
