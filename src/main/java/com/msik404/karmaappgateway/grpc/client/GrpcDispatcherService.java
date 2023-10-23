package com.msik404.karmaappgateway.grpc.client;

import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;
import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import com.msik404.karmaappgateway.grpc.client.encoding.ExceptionDecoder;
import com.msik404.karmaappgateway.grpc.client.encoding.exception.BadEncodingException;
import com.msik404.karmaappgateway.grpc.client.exception.InternalServerErrorException;
import com.msik404.karmaappgateway.grpc.client.exception.UnsupportedRoleException;
import com.msik404.karmaappgateway.grpc.client.exception.UnsupportedVisibilityException;
import com.msik404.karmaappgateway.grpc.client.mapper.*;
import com.msik404.karmaappgateway.grpc.client.zipper.PostDtoZipper;
import com.msik404.karmaappgateway.post.dto.PostDto;
import com.msik404.karmaappgateway.post.dto.PostRatingResponse;
import com.msik404.karmaappgateway.post.dto.PostWithImageDataDto;
import com.msik404.karmaappgateway.post.dto.Visibility;
import com.msik404.karmaappgateway.post.exception.FileProcessingException;
import com.msik404.karmaappgateway.post.exception.ImageNotFoundException;
import com.msik404.karmaappgateway.post.exception.PostNotFoundException;
import com.msik404.karmaappgateway.post.exception.RatingNotFoundException;
import com.msik404.karmaappgateway.user.Role;
import com.msik404.karmaappgateway.user.UserDetailsImpl;
import com.msik404.karmaappgateway.user.exception.DuplicateEmailException;
import com.msik404.karmaappgateway.user.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappgateway.user.exception.DuplicateUsernameException;
import com.msik404.karmaappgateway.user.exception.UserNotFoundException;
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
            @NonNull ExecutionException ex) throws BadEncodingException {

        var statusRuntimeException = (StatusRuntimeException) ex.getCause();
        String encodedException = statusRuntimeException.getMessage();

        return ExceptionDecoder.decodeException(encodedException);
    }

    @NonNull
    private List<PostDto> loadUsernames(
            @NonNull List<Post> posts
    ) throws ExecutionException, InterruptedException {

        Map<String, List<Integer>> userIdHexStringToPostIndices = new HashMap<>();
        for (int i = 0; i < posts.size(); i++) {
            String userIdHexString = posts.get(i).getUserId().getHexString();
            userIdHexStringToPostIndices
                    .computeIfAbsent(userIdHexString, key -> new ArrayList<>())
                    .add(i);
        }

        var usernamesRequestBuilder = UsernamesRequest.newBuilder();
        for (Map.Entry<String, List<Integer>> entry : userIdHexStringToPostIndices.entrySet()) {
            usernamesRequestBuilder.addUserIdHexStrings(entry.getKey());
        }

        // usernamesRequest userId hexString map to usernamesResponse usernames in the same order
        var usernamesRequest = usernamesRequestBuilder.build();

        UsernamesResponse usernamesResponse = usersStub.findUsernames(usernamesRequest).get();
        List<String> usernames = usernamesResponse.getUsernamesList();

        // Output is the same size as input. If some username for a given userId was not found an empty string is places in its place
        List<PostDto> postsWithUsernames = new ArrayList<>(Collections.nCopies(posts.size(), null));
        for (int i = 0; i < usernamesRequest.getUserIdHexStringsCount(); i++) {
            String userIdHexString = usernamesRequest.getUserIdHexStrings(i);

            List<Integer> postIndices = userIdHexStringToPostIndices.get(userIdHexString);
            for (int postIdx : postIndices) {
                Post post = posts.get(postIdx);
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
            @NonNull PostsRequest request
    ) throws InternalServerErrorException, BadEncodingException, UnsupportedVisibilityException {

        try {
            // get posts by request from posts microservice
            PostsResponse postsResponse = postsStub.findPosts(request).get();
            List<Post> posts = postsResponse.getPostsList();
            if (posts.isEmpty()) {
                return List.of();
            }

            return loadUsernames(posts);

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public List<PostDto> fetchPostsWithUsernames(
            @NonNull PostsRequest postsRequest,
            @NonNull UserIdRequest creatorIdRequest
    ) throws InternalServerErrorException, BadEncodingException, UnsupportedVisibilityException, UserNotFoundException {

        try {
            // get userId by username from users microservice
            MongoObjectId creatorId = usersStub.findUserId(creatorIdRequest).get();

            var postsWithCreatorIdRequest = PostsWithCreatorIdRequest.newBuilder()
                    .setPostsRequest(postsRequest)
                    .setCreatorId(MongoObjectIdMapper.mapToPostsMongoObjectId(creatorId))
                    .build();

            // get posts by PostsRequest and with given creatorId equal to userId
            PostsResponse response = postsStub.findPostsWithCreatorId(postsWithCreatorIdRequest).get();
            List<Post> posts = response.getPostsList();
            if (posts.isEmpty()) {
                return List.of();
            }

            return PostDtoZipper.zipIntoPostsWithUsernames(posts, creatorIdRequest.getUsername());

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public List<PostDto> fetchPostsWithUsernames(
            @NonNull PostsWithCreatorIdRequest request
    ) throws InternalServerErrorException, BadEncodingException, UnsupportedVisibilityException, UserNotFoundException {

        try {
            // async request for posts to posts microservice
            ListenableFuture<PostsResponse> postsResponseFuture = postsStub.findPostsWithCreatorId(request);

            var usernameRequest = UsernameRequest.newBuilder()
                    .setUserId(MongoObjectIdMapper.mapToUsersMongoObjectId(request.getCreatorId()))
                    .build();

            // async request for username to users microservice
            ListenableFuture<UsernameResponse> usernameResponseFuture = usersStub.findUsername(usernameRequest);

            // wait for responses
            PostsResponse postsResponse = postsResponseFuture.get();

            // if empty return and cancel username request
            List<Post> posts = postsResponse.getPostsList();
            if (posts.isEmpty()) {
                usernameResponseFuture.cancel(true);
                return List.of();
            }

            UsernameResponse usernameResponse = usernameResponseFuture.get();

            return PostDtoZipper.zipIntoPostsWithUsernames(
                    posts,
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
            @NonNull PostRequest postRequest
    ) throws InternalServerErrorException, BadEncodingException, PostNotFoundException, UserNotFoundException {

        try {
            PostWithImageData post = postsStub.findPostWithImageData(postRequest).get();

            var usernameRequest = UsernameRequest.newBuilder()
                    .setUserId(MongoObjectIdMapper.mapToUsersMongoObjectId(post.getPost().getUserId()))
                    .build();

            UsernameResponse usernameResponse = usersStub.findUsername(usernameRequest).get();

            return PostDtoMapper.map(post, usernameResponse.getUsername());

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public List<PostRatingResponse> fetchRatings(
            @NonNull PostRatingsRequest request
    ) throws InternalServerErrorException, BadEncodingException, UnsupportedVisibilityException {

        try {
            PostRatingsResponse response = postsStub.findPostRatings(request).get();

            return response.getPostRatingsList().stream().map(PostRatingMapper::map).toList();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public List<PostRatingResponse> fetchRatings(
            @NonNull PostRatingsRequest ratingsRequest,
            @NonNull UserIdRequest creatorIdRequest
    ) throws InternalServerErrorException, BadEncodingException, UnsupportedVisibilityException, UserNotFoundException {

        try {
            MongoObjectId creatorIdResponse = usersStub.findUserId(creatorIdRequest).get();

            var request = PostRatingsWithCreatorIdRequest.newBuilder()
                    .setPostsRatingsRequest(ratingsRequest)
                    .setCreatorId(MongoObjectIdMapper.mapToPostsMongoObjectId(creatorIdResponse))
                    .build();

            PostRatingsResponse response = postsStub.findPostRatingsWithCreatorId(request).get();

            return response.getPostRatingsList().stream().map(PostRatingMapper::map).toList();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public byte[] fetchImage(
            @NonNull ImageRequest request
    ) throws InternalServerErrorException, BadEncodingException, ImageNotFoundException {

        try {
            ImageResponse imageResponse = postsStub.findImage(request).get();

            return imageResponse.getImageData().toByteArray();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    public void createPost(
            @NonNull CreatePostRequest request
    ) throws InternalServerErrorException, BadEncodingException, FileProcessingException {

        try {
            postsStub.createPost(request).get();
        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    public int ratePost(
            @NonNull RatePostRequest request
    ) throws InternalServerErrorException, BadEncodingException, PostNotFoundException, RatingNotFoundException {

        try {
            ChangedRatingResponse response = postsStub.ratePost(request).get();

            return response.getDelta();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    public int unratePost(
            @NonNull UnratePostRequest request
    ) throws InternalServerErrorException, BadEncodingException, PostNotFoundException {

        try {
            ChangedRatingResponse response = postsStub.unratePost(request).get();

            return response.getDelta();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    public void changePostVisibility(
            @NonNull ChangePostVisibilityRequest request
    ) throws InternalServerErrorException, BadEncodingException, UnsupportedVisibilityException, PostNotFoundException {

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
            @NonNull PostCreatorIdRequest request
    ) throws InternalServerErrorException, BadEncodingException, PostNotFoundException {

        try {
            PostCreatorIdResponse response = postsStub.findPostCreatorId(request).get();

            return response.getUserId().getHexString();

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public Role fetchUserRole(
            @NonNull UserRoleRequest request
    ) throws InternalServerErrorException, BadEncodingException, UserNotFoundException {

        try {
            UserRoleResponse response = usersStub.findUserRole(request).get();

            return RoleMapper.map(response.getRole());

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    @NonNull
    public UserDetailsImpl fetchUserCredentials(
            @NonNull CredentialsRequest request
    ) throws InternalServerErrorException, BadEncodingException, UnsupportedRoleException, UserNotFoundException {

        try {
            CredentialsResponse response = usersStub.findCredentials(request).get();

            return CredentialsMapper.map(response);

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    public void createUser(
            @NonNull CreateUserRequest request
    ) throws InternalServerErrorException, BadEncodingException, UnsupportedRoleException, DuplicateUsernameException,
            DuplicateEmailException, DuplicateUnexpectedFieldException {

        try {
            usersStub.createUser(request).get();
        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }


    public void updateUser(
            @NonNull UpdateUserRequest request
    ) throws InternalServerErrorException, BadEncodingException, UnsupportedRoleException, DuplicateUsernameException,
            DuplicateEmailException, DuplicateUnexpectedFieldException {

        try {
            usersStub.updateUser(request).get();
        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

    public Visibility fetchPostVisibility(
            @NonNull com.msik404.karmaappposts.grpc.MongoObjectId request
    ) throws UnsupportedVisibilityException, PostNotFoundException {

        try {
            PostVisibilityResponse response = postsStub.findPostVisibility(request).get();

            return VisibilityMapper.map(response.getVisibility());

        } catch (InterruptedException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw decodeGrpcException(ex);
        }
    }

}
