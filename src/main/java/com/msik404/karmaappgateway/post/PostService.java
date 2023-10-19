package com.msik404.karmaappgateway.post;

import java.io.IOException;
import java.util.List;
import java.util.OptionalDouble;

import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import com.msik404.karmaappgateway.grpc.client.GrpcService;
import com.msik404.karmaappgateway.grpc.client.exception.InternalRestException;
import com.msik404.karmaappgateway.post.cache.PostRedisCache;
import com.msik404.karmaappgateway.post.cache.PostRedisCacheHandlerService;
import com.msik404.karmaappgateway.post.dto.*;
import com.msik404.karmaappgateway.post.exception.FileProcessingException;
import com.msik404.karmaappgateway.post.exception.ImageNotFoundException;
import com.msik404.karmaappgateway.user.Role;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostService {

    private final GrpcService grpcService;

    private final PostRedisCache cache;
    private final PostRedisCacheHandlerService cacheHandler;

    // todo: break down these classes so that there are no nullable parameters
    @NonNull
    public List<PostDto> findPaginatedPosts(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable ScrollPosition scrollPosition,
            @Nullable String creatorUsername
    ) throws RestFromGrpcException, InternalRestException {

        List<PostDto> results;

        if (creatorUsername == null) {
            if (scrollPosition == null) {
                results = cacheHandler.findTopNHandler(size, visibilities);
            } else {
                results = cacheHandler.findNextNHandler(size, visibilities, scrollPosition);
            }
        } else {
            if (scrollPosition == null) {
                results = grpcService.findTopNPostsByCreatorUsername(size, visibilities, creatorUsername);
            } else {
                results = grpcService.findNextNPostsByCreatorUsername(
                        size, visibilities, scrollPosition, creatorUsername);
            }
        }

        return results;
    }

    @NonNull
    public List<PostDto> findPaginatedOwnedPosts(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable ScrollPosition scrollPosition
    ) throws RestFromGrpcException, InternalRestException {

        // controller authentication objects come from filter and are UsernamePasswordAuthenticationToken.
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var clientId = (ObjectId) authentication.getPrincipal();

        List<PostDto> results;

        if (scrollPosition == null) {
            results = grpcService.findTopNPostsByCreatorId(size, visibilities, clientId);
        } else {
            results = grpcService.findNextNPostsByCreatorId(
                    size, visibilities, clientId, scrollPosition);
        }

        return results;
    }

    @NonNull
    public List<PostRatingResponse> findPaginatedPostRatings(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable ScrollPosition scrollPosition,
            @Nullable String creatorUsername
    ) throws RestFromGrpcException, InternalRestException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var clientId = (ObjectId) authentication.getPrincipal();

        List<PostRatingResponse> results;

        if (creatorUsername == null) {
            if (scrollPosition == null) {
                results = grpcService.findTopNRatings(size, visibilities, clientId);
            } else {
                results = grpcService.findNextNRatings(size, visibilities, clientId, scrollPosition);
            }
        } else {
            if (scrollPosition == null) {
                results = grpcService.findTopNRatingsByCreatorUsername(
                        size, visibilities, clientId, creatorUsername);
            } else {
                results = grpcService.findNextNRatingsByCreatorUsername(
                        size, visibilities, clientId, scrollPosition, creatorUsername);
            }
        }

        return results;
    }

    @NonNull
    public byte[] findImageByPostId(
            @NonNull ObjectId postId
    ) throws RestFromGrpcException, InternalRestException {

        return cache.getCachedImage(postId).orElseGet(() -> {
            final byte[] imageData = grpcService.findImage(postId);
            if (imageData.length == 0) {
                throw new ImageNotFoundException();
            }
            cache.cacheImage(postId, imageData);
            return imageData;
        });
    }

    public void create(
            @NonNull PostCreationRequest request,
            @NonNull MultipartFile image
    ) throws RestFromGrpcException, InternalRestException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var clientId = (ObjectId) authentication.getPrincipal();

        try {
            if (!image.isEmpty()) {
                final byte[] imageData = image.getBytes();
                grpcService.createPost(clientId, request, imageData);
                cache.cacheImage(clientId, imageData);
            }
        } catch (IOException ex) {
            throw new FileProcessingException();
        }
    }

    public void rate(
            @NonNull ObjectId postId,
            boolean isNewRatingPositive
    ) throws RestFromGrpcException, InternalRestException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var clientId = (ObjectId) authentication.getPrincipal();

        final int delta = grpcService.ratePost(postId, clientId, isNewRatingPositive);

        if (delta == 0) { // there is no point in updating cached score if delta is zero.
            return;
        }

        final OptionalDouble optionalNewKarmaScore = cache.updateKarmaScoreIfPresent(postId, delta);
        if (optionalNewKarmaScore.isEmpty()) { // this means that this post is not cached
            cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
        }
    }

    public void unrate(
            @NonNull ObjectId postId
    ) throws RestFromGrpcException, InternalRestException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var clientId = (ObjectId) authentication.getPrincipal();

        final int delta = grpcService.unratePost(postId, clientId);

        if (delta == 0) { // there is no point in updating cached score if delta is zero.
            return;
        }

        OptionalDouble optionalNewKarmaScore = cache.updateKarmaScoreIfPresent(postId, delta);
        if (optionalNewKarmaScore.isEmpty()) { // this means that this post is not cached
            cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
        }
    }

    public void changeVisibility(
            @NonNull ObjectId postId,
            @NonNull Visibility visibility
    ) throws RestFromGrpcException, InternalRestException {

        grpcService.changePostVisibility(postId, visibility);

        if (visibility.equals(Visibility.ACTIVE)) { // if this post was made active it might be high enough karma score
            cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
        } else {
            cache.deletePostFromCache(postId);
        }
    }

    public void changeOwnedPostVisibility(
            @NonNull ObjectId postId,
            @NonNull Visibility visibility
    ) throws AccessDeniedException, RestFromGrpcException, InternalRestException {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var clientId = (ObjectId) authentication.getPrincipal();

        // todo: there is a lot of error handling to be done
        final PostWithImageDataDto post = grpcService.findByPostId(postId);

        if (!clientId.equals(post.postDto().getUserId())) {
            throw new AccessDeniedException("Access denied");
        }

        final boolean isVisibilityDeleted = post.postDto().getVisibility().equals(Visibility.DELETED);
        final boolean isUserAdmin = authentication.getAuthorities().contains(
                new SimpleGrantedAuthority(Role.ADMIN.name()));

        if (isVisibilityDeleted && !isUserAdmin) {
            throw new AccessDeniedException("Access denied");
        }

        grpcService.changePostVisibility(postId, visibility);

        // if this post was made active it might have high enough karma score to be cached
        if (visibility.equals(Visibility.ACTIVE)) {
            cacheHandler.loadToCacheIfKarmaScoreIsHighEnough(post);
        } else {
            cache.deletePostFromCache(post.postDto().getId());
        }

    }
}
