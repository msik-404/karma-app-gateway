package com.msik404.karmaappgateway.post.comparator;

import org.bson.types.ObjectId;

public interface ComparablePost {
    ObjectId getId();

    Long getKarmaScore();
}
