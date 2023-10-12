package com.msik404.karmaappgateway.grpc.client.mapper;

import org.springframework.lang.NonNull;

public class MongoObjectIdMapper {

    @NonNull
    public static com.msik404.karmaappusers.grpc.MongoObjectId mapToUsersMongoObjectId(@NonNull String idHexString) {
        return com.msik404.karmaappusers.grpc.MongoObjectId.newBuilder().setHexString(idHexString).build();
    }

    @NonNull
    public static com.msik404.karmaappposts.grpc.MongoObjectId mapToPostsMongoObjectId(@NonNull String idHexString) {
        return com.msik404.karmaappposts.grpc.MongoObjectId.newBuilder().setHexString(idHexString).build();
    }

}
