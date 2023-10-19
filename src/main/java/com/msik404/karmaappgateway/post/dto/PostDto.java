package com.msik404.karmaappgateway.post.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.msik404.karmaappgateway.deserializer.ToObjectIdDeserializer;
import com.msik404.karmaappgateway.grpc.client.mapper.VisibilityMapper;
import com.msik404.karmaappgateway.post.comparator.ComparablePost;
import com.msik404.karmaappposts.grpc.PostVisibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostDto implements ComparablePost {

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = ToObjectIdDeserializer.class)
    private ObjectId id;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = ToObjectIdDeserializer.class)
    private ObjectId userId;

    private String username;
    private String headline;
    private String text;
    private Long karmaScore;
    private Visibility visibility;

    public PostDto(
            @NonNull ObjectId id,
            @NonNull ObjectId userId,
            @NonNull String username,
            @Nullable String headline,
            @Nullable String text,
            long karmaScore,
            @NonNull PostVisibility visibility) {

        this.id = id;
        this.userId = userId;
        this.username = username;
        this.headline = headline;
        this.text = text;
        this.karmaScore = karmaScore;
        this.visibility = VisibilityMapper.map(visibility);
    }

}
