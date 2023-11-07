package com.msik404.karmaappgateway.grpc.client.mapper;

import com.msik404.grpc.mongo.id.ProtoObjectId;
import com.msik404.karmaappgateway.post.dto.ScrollPosition;
import org.springframework.lang.NonNull;

public class ScrollPositionMapper {

    @NonNull
    public static com.msik404.karmaappposts.grpc.ScrollPosition map(@NonNull ScrollPosition scrollPosition) {

        return com.msik404.karmaappposts.grpc.ScrollPosition.newBuilder()
                .setPostId(ProtoObjectId.newBuilder().setHexString(scrollPosition.postId().toHexString()).build())
                .setKarmaScore(scrollPosition.karmaScore())
                .build();
    }

}
