package com.msik404.karmaappgateway;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.msik404.karmaappgateway.post.dto.PostDto;
import com.msik404.karmaappgateway.post.dto.Visibility;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;

public class TestingDataGenerator {

    @NonNull
    public static ObjectId getId(long id) {

        return new ObjectId(String.format("%024d", id));
    }

    @NonNull
    private static String getPostKey(@NonNull ObjectId postId) {
        return String.format("post:%s", postId.toHexString());
    }

    @NonNull
    public static PostDto getPostDtoForTesting(
            @NonNull long userIdLong,
            @NonNull long postIdLong,
            long karmaScore) {

        final ObjectId postId = getId(postIdLong);
        final ObjectId userId = getId(userIdLong);

        final String postKey = getPostKey(postId);

        return new PostDto(
                postId,
                userId,
                postKey,
                postKey,
                postKey,
                karmaScore,
                Visibility.ACTIVE
        );
    }

    /**
     * This comparator is made so to mimic redis ordered set reversed range retrieval order.
     */
    public static class CachedPostComparator implements Comparator<PostDto> {

        @Override
        public int compare(@NonNull PostDto postOne, @NonNull PostDto postTwo) {

            if (postOne.getKarmaScore().equals(postTwo.getKarmaScore())) {
                final String postKeyOne = getPostKey(postOne.getId());
                final String postKeyTwo = getPostKey(postTwo.getId());
                return -postKeyOne.compareTo(postKeyTwo);
            }
            return -postOne.getKarmaScore().compareTo(postTwo.getKarmaScore());
        }

    }

    @NonNull
    public static List<PostDto> getPostsForTesting() {

        final int postsAmount = 9;
        List<PostDto> posts = new ArrayList<>(postsAmount);

        final long userOneId = 1;
        posts.add(getPostDtoForTesting(userOneId, 1, 4));
        posts.add(getPostDtoForTesting(userOneId, 2, -1));
        posts.add(getPostDtoForTesting(userOneId, 3, 5));
        posts.add(getPostDtoForTesting(userOneId, 4, 5));
        posts.add(getPostDtoForTesting(userOneId, 5, 6));

        final long userTwoId = 2;
        posts.add(getPostDtoForTesting(userTwoId, 6, 3));
        posts.add(getPostDtoForTesting(userTwoId, 7, 2));
        posts.add(getPostDtoForTesting(userTwoId, 8, 4));
        posts.add(getPostDtoForTesting(userTwoId, 9, 0));

        posts.sort(new CachedPostComparator());

        return posts;
    }

}
