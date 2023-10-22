package com.msik404.karmaappgateway.grpc.client.mapper;

import com.msik404.karmaappgateway.grpc.client.exception.UnsupportedVisibilityException;
import com.msik404.karmaappgateway.post.dto.Visibility;
import com.msik404.karmaappposts.grpc.PostVisibility;
import org.springframework.lang.NonNull;

public class VisibilityMapper {

    @NonNull
    public static Visibility map(@NonNull PostVisibility visibility) throws UnsupportedVisibilityException {

        return switch (visibility) {
            case VIS_ACTIVE -> Visibility.ACTIVE;
            case VIS_HIDDEN -> Visibility.HIDDEN;
            case VIS_DELETED -> Visibility.DELETED;
            default -> throw new UnsupportedVisibilityException();
        };
    }

    @NonNull
    public static PostVisibility map(@NonNull Visibility visibility) {

        return switch (visibility) {
            case ACTIVE -> PostVisibility.VIS_ACTIVE;
            case HIDDEN -> PostVisibility.VIS_HIDDEN;
            case DELETED -> PostVisibility.VIS_DELETED;
        };
    }
}
