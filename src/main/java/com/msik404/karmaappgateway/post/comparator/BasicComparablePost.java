package com.msik404.karmaappgateway.post.comparator;

import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;

public record BasicComparablePost(@NonNull ObjectId id, long karmaScore) implements ComparablePost {

    @Override
    public ObjectId getId() {
        return id;
    }

    @Override
    public Long getKarmaScore() {
        return karmaScore;
    }
}
