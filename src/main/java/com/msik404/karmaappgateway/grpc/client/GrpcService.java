package com.msik404.karmaappgateway.grpc.client;

import java.util.Collection;
import java.util.List;

import com.google.protobuf.ByteString;
import com.msik404.karmaappgateway.auth.dto.RegisterRequest;
import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import com.msik404.karmaappgateway.grpc.client.exception.InternalRestException;
import com.msik404.karmaappgateway.grpc.client.mapper.MongoObjectIdMapper;
import com.msik404.karmaappgateway.grpc.client.mapper.RoleMapper;
import com.msik404.karmaappgateway.grpc.client.mapper.ScrollPositionMapper;
import com.msik404.karmaappgateway.grpc.client.mapper.VisibilityMapper;
import com.msik404.karmaappgateway.post.dto.ScrollPosition;
import com.msik404.karmaappgateway.post.dto.*;
import com.msik404.karmaappgateway.user.Role;
import com.msik404.karmaappgateway.user.UserDetailsImpl;
import com.msik404.karmaappposts.grpc.*;
import com.msik404.karmaappusers.grpc.CreateUserRequest;
import com.msik404.karmaappusers.grpc.CredentialsRequest;
import com.msik404.karmaappusers.grpc.UserIdRequest;
import com.msik404.karmaappusers.grpc.UserRoleRequest;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * This class accepts this microservice specific classes and transforms them into grpc requests and runs them.
 */
@Service
@RequiredArgsConstructor
public class GrpcService {

    private final GrpcDispatcherService dispatcher;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @NonNull
    public List<PostDto> findTopNPosts(
            int size,
            @NonNull Collection<Visibility> visibilities
    ) throws RestFromGrpcException, InternalRestException {

        final var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .build();

        return dispatcher.fetchPostsWithUsernames(postsRequest);
    }

    @NonNull
    public List<PostDto> findNextNPosts(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ScrollPosition scrollPosition
    ) throws RestFromGrpcException, InternalRestException {

        final var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .setPosition(ScrollPositionMapper.map(scrollPosition))
                .build();

        return dispatcher.fetchPostsWithUsernames(postsRequest);
    }

    @NonNull
    public List<PostDto> findTopNPostsByCreatorUsername(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull String creatorUsername
    ) throws RestFromGrpcException, InternalRestException {

        final var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .build();

        final var userIdRequest = UserIdRequest.newBuilder()
                .setUsername(creatorUsername)
                .build();

        return dispatcher.fetchPostsWithUsernames(postsRequest, userIdRequest);
    }

    @NonNull
    public List<PostDto> findNextNPostsByCreatorUsername(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ScrollPosition scrollPosition,
            @NonNull String creatorUsername
    ) throws RestFromGrpcException, InternalRestException {

        final var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .setPosition(ScrollPositionMapper.map(scrollPosition))
                .build();

        final var userIdRequest = UserIdRequest.newBuilder()
                .setUsername(creatorUsername)
                .build();

        return dispatcher.fetchPostsWithUsernames(postsRequest, userIdRequest);
    }

    @NonNull
    public List<PostDto> findTopNPostsByCreatorId(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId creatorId
    ) throws RestFromGrpcException, InternalRestException {

        final var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .build();

        final var postsWithCreatorIdRequest = PostsWithCreatorIdRequest.newBuilder()
                .setPostsRequest(postsRequest)
                .setCreatorId(MongoObjectIdMapper.mapToPostsMongoObjectId(creatorId))
                .build();

        return dispatcher.fetchPostsWithUsernames(postsWithCreatorIdRequest);
    }

    @NonNull
    public List<PostDto> findNextNPostsByCreatorId(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId creatorId,
            @NonNull ScrollPosition scrollPosition
    ) throws RestFromGrpcException, InternalRestException {

        final var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .setPosition(ScrollPositionMapper.map(scrollPosition))
                .build();

        final var postsWithCreatorIdRequest = PostsWithCreatorIdRequest.newBuilder()
                .setPostsRequest(postsRequest)
                .setCreatorId(MongoObjectIdMapper.mapToPostsMongoObjectId(creatorId))
                .build();

        return dispatcher.fetchPostsWithUsernames(postsWithCreatorIdRequest);
    }

    @NonNull
    public PostWithImageDataDto findByPostId(
            @NonNull ObjectId postId
    ) throws RestFromGrpcException, InternalRestException {

        final var postRequest = PostRequest.newBuilder()
                .setPostId(MongoObjectIdMapper.mapToPostsMongoObjectId(postId))
                .build();

        return dispatcher.fetchPostWithImage(postRequest);
    }

    @NonNull
    public List<PostRatingResponse> findTopNRatings(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId clientId
    ) throws RestFromGrpcException, InternalRestException {

        final var postRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .build();

        final var ratingsRequest = PostRatingsRequest.newBuilder()
                .setPostsRequest(postRequest)
                .setClientId(MongoObjectIdMapper.mapToPostsMongoObjectId(clientId))
                .build();

        return dispatcher.fetchRatings(ratingsRequest);
    }

    @NonNull
    public List<PostRatingResponse> findNextNRatings(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId clientId,
            @NonNull ScrollPosition scrollPosition
    ) throws RestFromGrpcException, InternalRestException {

        final var postRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .setPosition(ScrollPositionMapper.map(scrollPosition))
                .build();

        final var ratingsRequest = PostRatingsRequest.newBuilder()
                .setPostsRequest(postRequest)
                .setClientId(MongoObjectIdMapper.mapToPostsMongoObjectId(clientId))
                .build();

        return dispatcher.fetchRatings(ratingsRequest);
    }

    @NonNull
    public List<PostRatingResponse> findTopNRatingsByCreatorUsername(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId clientId,
            @NonNull String creatorUsername
    ) throws RestFromGrpcException, InternalRestException {

        final var postRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .build();

        final var ratingsRequest = PostRatingsRequest.newBuilder()
                .setPostsRequest(postRequest)
                .setClientId(MongoObjectIdMapper.mapToPostsMongoObjectId(clientId))
                .build();

        final var creatorIdRequest = UserIdRequest.newBuilder().setUsername(creatorUsername).build();

        return dispatcher.fetchRatings(ratingsRequest, creatorIdRequest);
    }

    @NonNull
    public List<PostRatingResponse> findNextNRatingsByCreatorUsername(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId clientId,
            @NonNull ScrollPosition scrollPosition,
            @NonNull String creatorUsername
    ) throws RestFromGrpcException, InternalRestException {

        final var postRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .setPosition(ScrollPositionMapper.map(scrollPosition))
                .build();

        final var ratingsRequest = PostRatingsRequest.newBuilder()
                .setPostsRequest(postRequest)
                .setClientId(MongoObjectIdMapper.mapToPostsMongoObjectId(clientId))
                .build();

        final var creatorIdRequest = UserIdRequest.newBuilder().setUsername(creatorUsername).build();

        return dispatcher.fetchRatings(ratingsRequest, creatorIdRequest);
    }

    @NonNull
    public byte[] findImage(
            @NonNull ObjectId postId
    ) throws RestFromGrpcException, InternalRestException {

        final var request = ImageRequest.newBuilder()
                .setPostId(MongoObjectIdMapper.mapToPostsMongoObjectId(postId))
                .build();

        return dispatcher.fetchImage(request);
    }

    public void createPost(
            @NonNull ObjectId clientId,
            @NonNull PostCreationRequest creationRequest,
            @Nullable byte[] imageData
    ) throws RestFromGrpcException, InternalRestException {

        final var requestBuilder = CreatePostRequest.newBuilder()
                .setUserId(MongoObjectId.newBuilder().setHexString(clientId.toHexString()).build())
                .setHeadline(creationRequest.headline())
                .setText(creationRequest.text());

        if (imageData != null) {
            requestBuilder.setImageData(ByteString.copyFrom(imageData));
        }

        dispatcher.createPost(requestBuilder.build());
    }

    public int ratePost(
            @NonNull ObjectId postId,
            @NonNull ObjectId clientId,
            boolean isPositive
    ) throws RestFromGrpcException, InternalRestException {

        final var request = RatePostRequest.newBuilder()
                .setPostId(MongoObjectIdMapper.mapToPostsMongoObjectId(postId))
                .setUserId(MongoObjectIdMapper.mapToPostsMongoObjectId(clientId))
                .setIsPositive(isPositive)
                .build();

        return dispatcher.ratePost(request);
    }

    public int unratePost(
            @NonNull ObjectId postId,
            @NonNull ObjectId clientId
    ) throws RestFromGrpcException, InternalRestException {

        final var request = UnratePostRequest.newBuilder()
                .setPostId(MongoObjectIdMapper.mapToPostsMongoObjectId(postId))
                .setUserId(MongoObjectIdMapper.mapToPostsMongoObjectId(clientId))
                .build();

        return dispatcher.unratePost(request);
    }

    public void changePostVisibility(
            @NonNull ObjectId postId,
            @NonNull Visibility visibility
    ) throws RestFromGrpcException, InternalRestException {

        final var request = ChangePostVisibilityRequest.newBuilder()
                .setPostId(MongoObjectIdMapper.mapToPostsMongoObjectId(postId))
                .setVisibility(VisibilityMapper.map(visibility))
                .build();

        dispatcher.changePostVisibility(request);
    }

    @NonNull
    public String findPostCreatorId(
            @NonNull ObjectId postId
    ) throws RestFromGrpcException, InternalRestException {

        final var request = PostCreatorIdRequest.newBuilder()
                .setPostId(MongoObjectIdMapper.mapToPostsMongoObjectId(postId))
                .build();

        return dispatcher.fetchPostCreatorId(request);
    }

    @NonNull
    public Role findUserRole(
            @NonNull ObjectId userId
    ) throws RestFromGrpcException, InternalRestException {

        final var request = UserRoleRequest.newBuilder()
                .setUserId(MongoObjectIdMapper.mapToUsersMongoObjectId(userId))
                .build();

        return dispatcher.fetchUserRole(request);
    }

    @NonNull
    public UserDetailsImpl findUserDetails(
            @NonNull String email
    ) throws RestFromGrpcException, InternalRestException {

        final var request = CredentialsRequest.newBuilder()
                .setEmail(email)
                .build();

        return dispatcher.fetchUserCredentials(request);
    }

    public void registerUser(
            @NonNull RegisterRequest registerRequest
    ) throws RestFromGrpcException, InternalRestException {

        final var requestBuilder = CreateUserRequest.newBuilder()
                .setEmail(registerRequest.email())
                .setUsername(registerRequest.username())
                .setPassword(bCryptPasswordEncoder.encode(registerRequest.password()))
                .setRole(RoleMapper.map(Role.USER));

        if (registerRequest.firstName() != null) {
            requestBuilder.setFirstName(registerRequest.firstName());
        }
        if (registerRequest.lastName() != null) {
            requestBuilder.setLastName(registerRequest.lastName());
        }

        dispatcher.createUser(requestBuilder.build());
    }

}
