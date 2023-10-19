package com.msik404.karmaappgateway.post.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.msik404.karmaappgateway.deserializer.ToObjectIdDeserializer;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.lang.Nullable;

public record PostRatingResponse(
        @NonNull
        @JsonSerialize(using = ToStringSerializer.class)
        @JsonDeserialize(using = ToObjectIdDeserializer.class)
        ObjectId id,

        @Nullable Boolean wasRatedPositively) {
}
