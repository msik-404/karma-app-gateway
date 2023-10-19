package com.msik404.karmaappgateway.grpc.client;

import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;
import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import com.msik404.karmaappgateway.grpc.client.encoding.ExceptionDecoder;
import com.msik404.karmaappgateway.grpc.client.encoding.exception.BadEncodingException;
import com.msik404.karmaappgateway.grpc.client.exception.InternalRestException;
import com.msik404.karmaappgateway.grpc.client.exception.InternalServerErrorException;
import com.msik404.karmaappgateway.grpc.client.mapper.*;
import com.msik404.karmaappgateway.grpc.client.zipper.PostDtoZipper;
import com.msik404.karmaappgateway.post.dto.PostDto;
import com.msik404.karmaappgateway.post.dto.PostRatingResponse;
import com.msik404.karmaappgateway.post.dto.PostWithImageDataDto;
import com.msik404.karmaappgateway.user.Role;
import com.msik404.karmaappgateway.user.UserDetailsImpl;
import com.msik404.karmaappposts.grpc.*;
import com.msik404.karmaappusers.grpc.MongoObjectId;
import com.msik404.karmaappusers.grpc.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * This class makes requests to microservices.
 */
@Service
@RequiredArgsConstructor
public class GrpcDispatcherService {

    private final PostsGrpc.PostsFutureStub postsStub;
    private final UsersGrpc.UsersFutureStub usersStub;

    @NonNull
    private static RestFromGrpcException decodeGrpcException(
            @NonNull final ExecutionException ex) throws BadEncodingException {

        final var statusRuntimeException = (StatusRuntimeException) ex.getCause();
        final String encodedException = statusRuntimeException.getMessage();

        return ExceptionDecoder.decodeException(encodedException);
    }

    @NonNull
    private List<PostDto> loadUsernames(
            @NonNull final List<Post> posts
    ) throws ExecutionException, InterruptedException {

        final Map<String, List<Integer>> userIdHexStringToPostIndices = new HashMap<>();
        for (int i = 0; i < posts.size(); i++) {
            final String userIdHexString = posts.get(i).getUserId().getHexString();
            userIdHexStringToPostIndices
                    .computeIfAbsent(userIdHexString, key -> new ArrayList<>())
                    .add(i);
        }

        final var usernamesRequestBuilder = UsernamesRequest.newBuilder();
        for (Map.Entry<String, List<Integer>> entry : userIdHexStringToPostIndices.entrySet()) {
            usernamesRequestBuilder.addUserIdHexStrings(entry.getKey());
        }

        // usernamesRequest userId hexString map to usernamesResponse usernames in the same order
        final var usernamesRequest = usernamesRequestBuilder.build();

        final UsernamesResponse usernamesResponse = usersStub.findUsernames(usernamesRequest).get();
        final List<String> usernames = usernamesResponse.getUsernamesList();

        // Output is the same size as input. If some username for a given userId was not found an empty string is places in its place
        final List<PostDto> postsWithUsernames = new ArrayList<>(Collections.nCopies(posts.size(), null));
        for (int i = 0; i < usernamesRequest.getUserIdHexStringsCount(); i++) {
            final String userIdHexString = usernamesRequest.getUserIdHexStrings(i);

            final List<Integer> postIndices = userIdHexStringToPostIndices.get(userIdHexString);
            for (int postIdx : postIndices) {
                final Post post = posts.get(postIdx);
                postsWithUsernames.set(postIdx, new PostDto(
                        new ObjectId(post.getPostId().getHexString()),
                        new ObjectId(post.getUserId().getHexString()),
                        usernames.get(i),
                        post.getHeadline(),
                        post.getText(),
                        post.getKarmaScore(),
                        post.getVisibility()
                ));
            }
        }
        return postsWithUsernames;
    }

    @NonNull
    public List<PostDto> fetchPostsWithUsernames(
            @NonNull final PostsRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            // get posts by request from posts microservice
            final PostsResponse postsResponse = postsStub.findPosts(request).get();
            final List<Post> posts = postsResponse.getPostsList();

            return loadUsernames(posts);

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public List<PostDto> fetchPostsWithUsernames(
            @NonNull final PostsRequest postsRequest,
            @NonNull final UserIdRequest creatorIdRequest
    ) throws RestFromGrpcException, InternalRestException {

        try {
            // get userId by username from users microservice
            final MongoObjectId creatorId = usersStub.findUserId(creatorIdRequest).get();

            final var postsWithCreatorIdRequest = PostsWithCreatorIdRequest.newBuilder()
                    .setPostsRequest(postsRequest)
                    .setCreatorId(MongoObjectIdMapper.mapToPostsMongoObjectId(creatorId))
                    .build();

            // get posts by PostsRequest and with given creatorId equal to userId
            final PostsResponse response = postsStub.findPostsWithCreatorId(postsWithCreatorIdRequest).get();

            return PostDtoZipper.zipIntoPostsWithUsernames(response.getPostsList(), creatorIdRequest.getUsername());

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public List<PostDto> fetchPostsWithUsernames(
            @NonNull final PostsWithCreatorIdRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            // async request for posts to posts microservice
            final ListenableFuture<PostsResponse> postsResponseFuture = postsStub.findPostsWithCreatorId(request);

            final var usernameRequest = UsernameRequest.newBuilder()
                    .setUserId(MongoObjectIdMapper.mapToUsersMongoObjectId(request.getCreatorId()))
                    .build();

            // async request for username to users microservice
            final ListenableFuture<UsernameResponse> usernameResponseFuture = usersStub.findUsername(usernameRequest);

            // wait for responses
            final PostsResponse postsResponse = postsResponseFuture.get();
            final UsernameResponse usernameResponse = usernameResponseFuture.get();

            return PostDtoZipper.zipIntoPostsWithUsernames(
                    postsResponse.getPostsList(),
                    usernameResponse.getUsername()
            );

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public PostWithImageDataDto fetchPostWithImage(
            @NonNull final PostRequest postRequest
    ) throws RestFromGrpcException, InternalRestException {

        try {
            final PostWithImageData post = postsStub.findPostWithImageData(postRequest).get();

            final var usernameRequest = UsernameRequest.newBuilder()
                    .setUserId(MongoObjectIdMapper.mapToUsersMongoObjectId(post.getPost().getUserId()))
                    .build();

            final UsernameResponse usernameResponse = usersStub.findUsername(usernameRequest).get();

            return PostDtoMapper.map(post, usernameResponse.getUsername());

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public List<PostRatingResponse> fetchRatings(
            @NonNull final PostRatingsRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            final PostRatingsResponse response = postsStub.findPostRatings(request).get();

            return response.getPostRatingsList().stream().map(PostRatingMapper::map).toList();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public List<PostRatingResponse> fetchRatings(
            @NonNull final PostRatingsRequest ratingsRequest,
            @NonNull final UserIdRequest creatorIdRequest
    ) throws RestFromGrpcException, InternalRestException {

        try {
            final MongoObjectId creatorIdResponse = usersStub.findUserId(creatorIdRequest).get();

            final var request = PostRatingsWithCreatorIdRequest.newBuilder()
                    .setPostsRatingsRequest(ratingsRequest)
                    .setCreatorId(MongoObjectIdMapper.mapToPostsMongoObjectId(creatorIdResponse))
                    .build();

            final PostRatingsResponse response = postsStub.findPostRatingsWithCreatorId(request).get();

            return response.getPostRatingsList().stream().map(PostRatingMapper::map).toList();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public byte[] fetchImage(
            @NonNull final ImageRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            final ImageResponse imageResponse = postsStub.findImage(request).get();

            return imageResponse.getImageData().toByteArray();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    public void createPost(
            @NonNull final CreatePostRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            postsStub.createPost(request).get();
        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    public int ratePost(
            @NonNull final RatePostRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            final ChangedRatingResponse response = postsStub.ratePost(request).get();

            return response.getDelta();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    public int unratePost(
            @NonNull final UnratePostRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            final ChangedRatingResponse response = postsStub.unratePost(request).get();

            return response.getDelta();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    public void changePostVisibility(
            @NonNull final ChangePostVisibilityRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            postsStub.changePostVisibility(request).get();
        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public String fetchPostCreatorId(
            @NonNull final PostCreatorIdRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            final PostCreatorIdResponse response = postsStub.findPostCreatorId(request).get();

            return response.getUserId().getHexString();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public Role fetchUserRole(
            @NonNull final UserRoleRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            final UserRoleResponse response = usersStub.findUserRole(request).get();

            return RoleMapper.map(response.getRole());

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public UserDetailsImpl fetchUserCredentials(
            @NonNull final CredentialsRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            final CredentialsResponse response = usersStub.findCredentials(request).get();

            return CredentialsMapper.map(response);

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    public void createUser(
            @NonNull final CreateUserRequest request
    ) throws RestFromGrpcException, InternalRestException {

        try {
            usersStub.createUser(request).get();
        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

}
