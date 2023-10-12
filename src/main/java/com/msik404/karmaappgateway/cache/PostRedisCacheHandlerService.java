package com.msik404.karmaappgateway.cache;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.msik404.karmaappgateway.comparator.PostComparator;
import com.msik404.karmaappgateway.dto.PostDto;
import com.msik404.karmaappgateway.dto.PostWithImageDataDto;
import com.msik404.karmaappgateway.dto.ScrollPosition;
import com.msik404.karmaappgateway.dto.Visibility;
import com.msik404.karmaappgateway.grpc.client.GrpcHandlerService;
import com.msik404.karmaappgateway.grpc.client.exception.InternalServerErrorException;
import com.msik404.karmaappgateway.grpc.client.mapper.MongoObjectIdMapper;
import com.msik404.karmaappgateway.grpc.client.mapper.PostDtoMapper;
import com.msik404.karmaappgateway.grpc.client.mapper.ScrollPositionMapper;
import com.msik404.karmaappgateway.grpc.client.mapper.VisibilityMapper;
import com.msik404.karmaappposts.grpc.*;
import com.msik404.karmaappusers.grpc.UsernameRequest;
import com.msik404.karmaappusers.grpc.UsernameResponse;
import com.msik404.karmaappusers.grpc.UsersGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostRedisCacheHandlerService {

    private static final int CACHED_POSTS_AMOUNT = 10_000;

    private final PostRedisCache cache;

    private final PostsGrpc.PostsFutureStub postsStub;
    private final UsersGrpc.UsersFutureStub usersStub;

    private final GrpcHandlerService grpcHandlerService;

    private static boolean isOnlyActive(@NonNull List<Visibility> visibilities) {
        return visibilities.size() == 1 && visibilities.contains(Visibility.ACTIVE);
    }

    @NonNull
    public List<PostDto> updateCache() throws InternalServerErrorException {

        final var postsRequest = PostsRequest.newBuilder()
                .setSize(CACHED_POSTS_AMOUNT)
                .addAllVisibilities(List.of(PostVisibility.VIS_ACTIVE))
                .build();

        final List<PostDto> newValuesForCache = grpcHandlerService.fetchPostsWithUsernames(postsRequest);

        cache.reinitializeCache(newValuesForCache);

        return newValuesForCache;
    }

    @NonNull
    public List<PostDto> findTopNHandler(
            int size,
            @NonNull List<Visibility> visibilities) throws InternalServerErrorException {

        final var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .build();

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                final List<PostDto> newValuesForCache = updateCache();
                final int endBound = Math.min(size, newValuesForCache.size());
                results = newValuesForCache.subList(0, endBound);
            } else {
                results = cache.findTopNCached(size).orElseGet(() -> grpcHandlerService.fetchPostsWithUsernames(postsRequest));
            }
        } else {
            results = grpcHandlerService.fetchPostsWithUsernames(postsRequest);
        }

        return results;
    }

    private int findNextSmallerThan(@NonNull List<PostDto> topPosts, @NonNull ScrollPosition scrollPosition) {

        final int value = Collections.binarySearch(
                topPosts,
                new PostDto(scrollPosition),
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

        final var postsRequest = PostsRequest.newBuilder()
                .setSize(CACHED_POSTS_AMOUNT)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .setPosition(ScrollPositionMapper.map(scrollPosition))
                .build();

        List<PostDto> results;

        if (isOnlyActive(visibilities)) {
            if (cache.isEmpty()) {
                final List<PostDto> newValuesForCache = updateCache();
                final int firstSmallerElementIdx = findNextSmallerThan(newValuesForCache, scrollPosition);

                final int endBound = Math.min(firstSmallerElementIdx + size, newValuesForCache.size());
                results = newValuesForCache.subList(firstSmallerElementIdx, endBound);
            } else {
                results = cache.findNextNCached(size, scrollPosition.karmaScore())
                        .orElseGet(() -> grpcHandlerService.fetchPostsWithUsernames(postsRequest));
            }
        } else {
            results = grpcHandlerService.fetchPostsWithUsernames(postsRequest);
        }

        return results;
    }

    public boolean loadToCacheIfKarmaScoreIsHighEnough(@NonNull PostWithImageDataDto post) {

        final Optional<Boolean> optionalIsHighEnough = cache.isKarmaScoreGreaterThanLowestScoreInZSet(
                post.postDto().getKarmaScore());

        if (optionalIsHighEnough.isEmpty()) {
            return false;
        }

        final boolean isHighEnough = optionalIsHighEnough.get();
        if (!isHighEnough) {
            return false;
        }

        return cache.insertPost(post.postDto(), post.imageData());
    }

    public boolean loadPostDataToCacheIfKarmaScoreIsHighEnough(String postIdHexString) {

        final var postRequest = PostRequest.newBuilder()
                .setPostId(MongoObjectIdMapper.mapToPostsMongoObjectId(postIdHexString))
                .build();

        try {
            // todo: if post not found return false
            final PostWithImageData post = postsStub.findPostWithImageData(postRequest).get();

            final String userIdHexString = post.getPost().getUserId().getHexString();

            final var usernameRequest = UsernameRequest.newBuilder()
                    .setUserId(MongoObjectIdMapper.mapToUsersMongoObjectId(userIdHexString))
                    .build();

            // todo: if username not found return false
            final UsernameResponse usernameResponse = usersStub.findUsername(usernameRequest).get();

            return loadToCacheIfKarmaScoreIsHighEnough(PostDtoMapper.map(post, usernameResponse.getUsername()));

        } catch (InterruptedException | ExecutionException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        }
    }

}