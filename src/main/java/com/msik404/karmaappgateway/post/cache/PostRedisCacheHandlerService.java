package com.msik404.karmaappgateway.post.cache;

import java.util.Collections;
import java.util.List;

import com.msik404.karmaappgateway.grpc.client.GrpcService;
import com.msik404.karmaappgateway.post.comparator.BasicComparablePost;
import com.msik404.karmaappgateway.post.comparator.PostComparator;
import com.msik404.karmaappgateway.post.dto.PostDto;
import com.msik404.karmaappgateway.post.dto.PostWithImageDataDto;
import com.msik404.karmaappgateway.post.dto.ScrollPosition;
import com.msik404.karmaappgateway.post.dto.Visibility;
import com.msik404.karmaappgateway.post.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostRedisCacheHandlerService {

    private final PostRedisCache cache;

    private final GrpcService grpcService;

    private static boolean isOnlyActive(@NonNull List<Visibility> visibilities) {
        return visibilities.size() == 1 && visibilities.contains(Visibility.ACTIVE);
    }

    @NonNull
    public List<PostDto> updateCache() {

        List<PostDto> newValuesForCache = grpcService.findTopNPosts(
                PostRedisCache.getMaxCachedPosts(),
                List.of(Visibility.ACTIVE)
        );

        if (!newValuesForCache.isEmpty()) {
            cache.reinitializeCache(newValuesForCache);
        }

        return newValuesForCache;
    }

    @NonNull
    public List<PostDto> findTopNHandler(
            int size,
            @NonNull List<Visibility> visibilities) {

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                List<PostDto> newValuesForCache = updateCache();
                int endBound = Math.min(size, newValuesForCache.size());
                results = newValuesForCache.subList(0, endBound);
            } else {
                results = cache.findTopNCached(size)
                        .orElseGet(() -> grpcService.findTopNPosts(size, visibilities));
            }
        } else {
            results = grpcService.findTopNPosts(size, visibilities);
        }

        return results;
    }

    private int findNextSmallerThan(@NonNull List<PostDto> topPosts, @NonNull ScrollPosition scrollPosition) {

        int value = Collections.binarySearch(
                topPosts,
                new BasicComparablePost(scrollPosition.postId(), scrollPosition.karmaScore()),
                new PostComparator().reversed()
        );

        // returns topPosts.size() if post with karmaScore is last or insertion point would be last
        if (value < 0) {
            // get insertion point
            return -value - 1;
        }
        return value + 1;
    }

    @NonNull
    public List<PostDto> findNextNHandler(
            int size,
            @NonNull List<Visibility> visibilities,
            @NonNull ScrollPosition scrollPosition) {

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                List<PostDto> newValuesForCache = updateCache();
                int firstSmallerElementIdx = findNextSmallerThan(newValuesForCache, scrollPosition);

                int endBound = Math.min(firstSmallerElementIdx + size, newValuesForCache.size());
                results = newValuesForCache.subList(firstSmallerElementIdx, endBound);
            } else {
                results = cache.findNextNCached(size, scrollPosition)
                        .orElseGet(() -> grpcService.findNextNPosts(size, visibilities, scrollPosition));
            }
        } else {
            results = grpcService.findNextNPosts(size, visibilities, scrollPosition);
        }

        return results;
    }

    /**
     * Post will be cached if less than CACHED_POSTS_AMOUNT posts are cached or input post score is higher than
     * the lowest cached post score. Because of this functionality there may be more cached posts than specified
     * amount allows, but I doubt that this would be problematic, because cache gets refreshed every PostRedisCache.TIMEOUT.
     *
     * @param post Input post with image data to be cached.
     * @return true if cached else false.
     */
    public boolean loadToCacheIfPossible(@NonNull PostWithImageDataDto post) {

        long cacheSize = cache.getZSetSize();

        if (cacheSize >= PostRedisCache.getMaxCachedPosts()) {

            boolean isAccepted = cache.isKarmaScoreGreaterThanLowestScoreInZSet(
                    post.postDto().getKarmaScore());

            if (!isAccepted) {
                return false;
            }
        }

        return cache.insertPost(post.postDto(), post.imageData());
    }

    /**
     * @param postId Id of post with image data if found to be cached.
     * @return true if cached else false.
     * @throws PostNotFoundException thrown when post with requested postId is not found.
     */
    public boolean loadPostDataToCacheIfPossible(
            @NonNull ObjectId postId
    ) throws PostNotFoundException {

        try {
            PostWithImageDataDto post = grpcService.findByPostId(postId);

            return loadToCacheIfPossible(post);

        } catch (PostNotFoundException ex) {
            return false;
        }
    }

}