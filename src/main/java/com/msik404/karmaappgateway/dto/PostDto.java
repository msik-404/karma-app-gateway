package com.msik404.karmaappgateway.dto;

import com.msik404.karmaappgateway.comparator.ComparablePost;
import com.msik404.karmaappgateway.grpc.client.mapper.VisibilityMapper;
import com.msik404.karmaappposts.grpc.PostVisibility;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Data
public class PostDto implements ComparablePost {

    private final String idHexString;
    private final String userIdHexString;
    private final String username;
    private final String headline;
    private final String text;
    private Long karmaScore;
    private final Visibility visibility;

    public PostDto(
            @NonNull String idHexString,
            @NonNull String userIdHexString,
            @NonNull String username,
            @Nullable String headline,
            @Nullable String text,
            long karmaScore,
            @NonNull PostVisibility visibility) {

        this.idHexString = idHexString;
        this.userIdHexString = userIdHexString;
        this.username = username;
        this.headline = headline;
        this.text = text;
        this.karmaScore = karmaScore;
        this.visibility = VisibilityMapper.map(visibility);
    }

    public PostDto(@NonNull ScrollPosition position) {
        // This may seem a little bit silly because it contradicts @NonNull annotations, but this strange constructor
        // is required for binary search in PostRedisCacheHandlerService.
        this(
                position.postIdHexString(),
                null,
                null,
                null,
                null,
                position.karmaScore(),
                null
        );
    }

}
