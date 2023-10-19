package com.msik404.karmaappgateway.grpc.client.mapper;

import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;

public class MongoObjectIdMapper {

    @NonNull
    public static com.msik404.karmaappusers.grpc.MongoObjectId mapToUsersMongoObjectId(@NonNull ObjectId id) {
        return com.msik404.karmaappusers.grpc.MongoObjectId.newBuilder().setHexString(id.toHexString()).build();
    }

    @NonNull
    public static com.msik404.karmaappposts.grpc.MongoObjectId mapToPostsMongoObjectId(@NonNull ObjectId id) {
        return com.msik404.karmaappposts.grpc.MongoObjectId.newBuilder().setHexString(id.toHexString()).build();
    }

    @NonNull
    public static com.msik404.karmaappusers.grpc.MongoObjectId mapToUsersMongoObjectId(
            @NonNull com.msik404.karmaappposts.grpc.MongoObjectId mongoObjectId) {

        return com.msik404.karmaappusers.grpc.MongoObjectId.newBuilder()
                .setHexString(mongoObjectId.getHexString())
                .build();
    }

    @NonNull
    public static com.msik404.karmaappposts.grpc.MongoObjectId mapToPostsMongoObjectId(
            @NonNull com.msik404.karmaappusers.grpc.MongoObjectId mongoObjectId) {

        return com.msik404.karmaappposts.grpc.MongoObjectId.newBuilder()
                .setHexString(mongoObjectId.getHexString())
                .build();
    }

}
