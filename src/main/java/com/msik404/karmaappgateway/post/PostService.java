package com.msik404.karmaappgateway.post;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.OptionalDouble;

import com.msik404.karmaappgateway.auth.exception.InsufficientRoleException;
import com.msik404.karmaappgateway.grpc.client.GrpcService;
import com.msik404.karmaappgateway.post.cache.PostRedisCache;
import com.msik404.karmaappgateway.post.cache.PostRedisCacheHandlerService;
import com.msik404.karmaappgateway.post.dto.*;
import com.msik404.karmaappgateway.post.exception.FileProcessingException;
import com.msik404.karmaappgateway.post.exception.ImageNotFoundException;
import com.msik404.karmaappgateway.post.exception.PostNotFoundException;
import com.msik404.karmaappgateway.post.exception.RatingNotFoundException;
import com.msik404.karmaappgateway.user.Role;
import com.msik404.karmaappgateway.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
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

    @NonNull
    public List<PostDto> findPaginatedPosts(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable ScrollPosition scrollPosition,
            @Nullable String creatorUsername
    ) throws UserNotFoundException {

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
    ) throws UserNotFoundException {

        // controller authentication objects come from filter and are UsernamePasswordAuthenticationToken.
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var clientId = (ObjectId) authentication.getPrincipal();

        List<PostDto> results;

        if (scrollPosition == null) {
            results = grpcService.findTopNPostsByCreatorId(size, visibilities, clientId);
        } else {
            results = grpcService.findNextNPostsByCreatorId(size, visibilities, clientId, scrollPosition);
        }

        return results;
    }

    @NonNull
    public List<PostRatingResponse> findPaginatedPostRatings(
            int size,
            @NonNull List<Visibility> visibilities,
            @Nullable ScrollPosition scrollPosition,
            @Nullable String creatorUsername
    ) throws UserNotFoundException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var clientId = (ObjectId) authentication.getPrincipal();

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
    ) throws ImageNotFoundException {

        return cache.getCachedImage(postId).orElseGet(() -> {
            byte[] imageData = grpcService.findImage(postId);
            if (imageData.length == 0) {
                throw new ImageNotFoundException();
            }
            cache.cacheImage(postId, imageData);
            return imageData;
        });
    }

    public void create(
            @NonNull PostCreationRequest request,
            @Nullable MultipartFile image) throws FileProcessingException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var clientId = (ObjectId) authentication.getPrincipal();

        try {
            byte[] imageData = null;
            if (image != null && !image.isEmpty()) {
                imageData = image.getBytes();
            }
            grpcService.createPost(clientId, request, imageData);
        } catch (IOException ex) {
            throw new FileProcessingException();
        }
    }

    public void rate(
            @NonNull ObjectId postId,
            boolean isNewRatingPositive
    ) throws PostNotFoundException, RatingNotFoundException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var clientId = (ObjectId) authentication.getPrincipal();

        int delta = grpcService.ratePost(postId, clientId, isNewRatingPositive);

        if (delta == 0) { // there is no point in updating cached score if delta is zero.
            return;
        }

        OptionalDouble optionalNewKarmaScore = cache.updateKarmaScoreIfPresent(postId, delta);
        if (optionalNewKarmaScore.isEmpty()) { // this means that this post is not cached
            cacheHandler.loadPostDataToCacheIfPossible(postId);
        }
    }

    public void unrate(
            @NonNull ObjectId postId
    ) throws PostNotFoundException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var clientId = (ObjectId) authentication.getPrincipal();

        int delta = grpcService.unratePost(postId, clientId);

        if (delta == 0) { // there is no point in updating cached score if delta is zero.
            return;
        }

        OptionalDouble optionalNewKarmaScore = cache.updateKarmaScoreIfPresent(postId, delta);
        if (optionalNewKarmaScore.isEmpty()) { // this means that this post is not cached
            cacheHandler.loadPostDataToCacheIfPossible(postId);
        }
    }

    public void changeVisibility(
            @NonNull ObjectId postId,
            @NonNull Visibility visibility
    ) throws InsufficientRoleException, PostNotFoundException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> clientAuthorities = authentication.getAuthorities();
        boolean isAdmin = clientAuthorities.contains(new SimpleGrantedAuthority(Role.ADMIN.name()));

        if (!isAdmin) {
            Visibility persistedVisibility = grpcService.findVisibility(postId);
            if (persistedVisibility.equals(Visibility.DELETED)) {
                throw new InsufficientRoleException(
                        "Access denied. You must be admin to change deleted post status to hidden status."
                );
            }
        }

        grpcService.changePostVisibility(postId, visibility);

        if (visibility.equals(Visibility.ACTIVE)) { // if this post was made active it might be high enough karma score
            cacheHandler.loadPostDataToCacheIfPossible(postId);
        } else {
            cache.deletePostFromCache(postId);
        }
    }

    public void changeOwnedPostVisibility(
            @NonNull ObjectId postId,
            @NonNull Visibility visibility
    ) throws InsufficientRoleException, UserNotFoundException, PostNotFoundException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var clientId = (ObjectId) authentication.getPrincipal();

        PostWithImageDataDto post = grpcService.findByPostId(postId);

        if (!clientId.equals(post.postDto().getUserId())) {
            throw new InsufficientRoleException("Access denied. You must be the owner of the post to hide|delete it.");
        }

        boolean isVisibilityDeleted = post.postDto().getVisibility().equals(Visibility.DELETED);
        boolean isUserAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority(Role.ADMIN.name()));

        if (isVisibilityDeleted && !isUserAdmin) {
            throw new InsufficientRoleException("Access denied. You must be Admin to activate deleted post.");
        }

        grpcService.changePostVisibility(postId, visibility);

        // if this post was made active it might have high enough karma score to be cached
        if (visibility.equals(Visibility.ACTIVE)) {
            cacheHandler.loadToCacheIfPossible(post);
        } else {
            cache.deletePostFromCache(post.postDto().getId());
        }

    }
}
