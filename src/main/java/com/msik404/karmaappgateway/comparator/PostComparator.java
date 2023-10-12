package com.msik404.karmaappgateway.comparator;

import java.util.Comparator;

import org.springframework.lang.NonNull;

public class PostComparator implements Comparator<ComparablePost> {

    @Override
    public int compare(@NonNull ComparablePost postOne, @NonNull ComparablePost postTwo) {

        if (postOne.getKarmaScore().equals(postTwo.getKarmaScore())) {
            return -postOne.getIdHexString().compareTo(postTwo.getIdHexString());
        }
        return postOne.getKarmaScore().compareTo(postTwo.getKarmaScore());
    }

}
