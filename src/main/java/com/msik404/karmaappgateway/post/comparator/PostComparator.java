package com.msik404.karmaappgateway.post.comparator;

import java.util.Comparator;

import org.springframework.lang.NonNull;

public class PostComparator implements Comparator<ComparablePost> {

    @Override
    public int compare(@NonNull ComparablePost postOne, @NonNull ComparablePost postTwo) {

        if (postOne.getKarmaScore().equals(postTwo.getKarmaScore())) {
            return -postOne.getId().compareTo(postTwo.getId());
        }
        return postOne.getKarmaScore().compareTo(postTwo.getKarmaScore());
    }

}
