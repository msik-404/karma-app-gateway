package com.msik404.karmaappgateway.grpc.client.mapper;

import com.msik404.karmaappgateway.dto.Visibility;
import com.msik404.karmaappgateway.grpc.client.exception.UnsupportedVisibilityException;
import com.msik404.karmaappposts.grpc.PostVisibility;
import org.springframework.lang.NonNull;

public class VisibilityMapper {

    @NonNull
    public static Visibility map(@NonNull PostVisibility visibility) throws UnsupportedVisibilityException {

        switch (visibility) {
            case VIS_ACTIVE -> {
                return Visibility.ACTIVE;
            }
            case VIS_HIDDEN -> {
                return Visibility.HIDDEN;
            }
            case VIS_DELETED -> {
                return Visibility.DELETED;
            }
            default -> throw new UnsupportedVisibilityException();
        }
    }

    @NonNull
    public static PostVisibility map(@NonNull Visibility visibility) {

        switch (visibility) {
            case ACTIVE -> {
                return PostVisibility.VIS_ACTIVE;
            }
            case HIDDEN -> {
                return PostVisibility.VIS_HIDDEN;
            }
            case DELETED -> {
                return PostVisibility.VIS_DELETED;
            }
            default -> {
                return PostVisibility.UNRECOGNIZED;
            }
        }
    }
}
