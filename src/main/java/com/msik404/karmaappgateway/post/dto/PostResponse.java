package com.msik404.karmaappgateway.post.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.msik404.karmaappgateway.deserializer.ToObjectIdDeserializer;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record PostResponse(

        @NonNull
        @JsonSerialize(using = ToStringSerializer.class)
        @JsonDeserialize(using = ToObjectIdDeserializer.class)
        ObjectId id,

        @NonNull
        String username,

        @Nullable
        String headline,

        @Nullable
        String text,

        long karmaScore,

        @NonNull
        Visibility visibility) {

    public PostResponse(@NonNull PostDto postDto) {

        this(
                postDto.getId(),
                postDto.getUsername(),
                postDto.getHeadline(),
                postDto.getText(),
                postDto.getKarmaScore(),
                postDto.getVisibility()
        );
    }
}
