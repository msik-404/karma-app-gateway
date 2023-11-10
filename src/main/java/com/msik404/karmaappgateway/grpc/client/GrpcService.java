package com.msik404.karmaappgateway.grpc.client;

import java.util.Collection;
import java.util.List;

import com.google.protobuf.ByteString;
import com.msik404.grpc.mongo.id.ProtoObjectId;
import com.msik404.karmaappgateway.auth.dto.RegisterRequest;
import com.msik404.karmaappgateway.grpc.client.exception.UnsupportedRoleException;
import com.msik404.karmaappgateway.grpc.client.exception.UnsupportedVisibilityException;
import com.msik404.karmaappgateway.grpc.client.mapper.RoleMapper;
import com.msik404.karmaappgateway.grpc.client.mapper.ScrollPositionMapper;
import com.msik404.karmaappgateway.grpc.client.mapper.UserUpdateMapper;
import com.msik404.karmaappgateway.grpc.client.mapper.VisibilityMapper;
import com.msik404.karmaappgateway.post.dto.ScrollPosition;
import com.msik404.karmaappgateway.post.dto.*;
import com.msik404.karmaappgateway.post.exception.FileProcessingException;
import com.msik404.karmaappgateway.post.exception.ImageNotFoundException;
import com.msik404.karmaappgateway.post.exception.PostNotFoundException;
import com.msik404.karmaappgateway.post.exception.RatingNotFoundException;
import com.msik404.karmaappgateway.user.Role;
import com.msik404.karmaappgateway.user.UserDetailsImpl;
import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaappgateway.user.exception.DuplicateEmailException;
import com.msik404.karmaappgateway.user.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappgateway.user.exception.DuplicateUsernameException;
import com.msik404.karmaappgateway.user.exception.UserNotFoundException;
import com.msik404.karmaappposts.grpc.*;
import com.msik404.karmaappusers.grpc.CreateUserRequest;
import com.msik404.karmaappusers.grpc.CredentialsRequest;
import com.msik404.karmaappusers.grpc.UserIdRequest;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * This class accepts this microservice specific classes and transforms them into grpc requests and runs them.
 */
@Service
public class GrpcService {

    private final GrpcDispatcherService dispatcher;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserUpdateMapper userUpdateMapper;

    public GrpcService(GrpcDispatcherService dispatcher, BCryptPasswordEncoder bCryptPasswordEncoder) {

        this.dispatcher = dispatcher;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;

        this.userUpdateMapper = new UserUpdateMapper(bCryptPasswordEncoder);
    }

    @NonNull
    public List<PostDto> findTopNPosts(
            int size,
            @NonNull Collection<Visibility> visibilities) {

        var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .build();

        return dispatcher.fetchPostsWithUsernames(postsRequest);
    }

    @NonNull
    public List<PostDto> findNextNPosts(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ScrollPosition scrollPosition) {

        var postsRequest = PostsRequest.newBuilder()
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
    ) throws UserNotFoundException {

        var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .build();

        var userIdRequest = UserIdRequest.newBuilder()
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
    ) throws UserNotFoundException {

        var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .setPosition(ScrollPositionMapper.map(scrollPosition))
                .build();

        var userIdRequest = UserIdRequest.newBuilder()
                .setUsername(creatorUsername)
                .build();

        return dispatcher.fetchPostsWithUsernames(postsRequest, userIdRequest);
    }

    @NonNull
    public List<PostDto> findTopNPostsByCreatorId(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId creatorId
    ) throws UserNotFoundException {

        var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .build();

        var postsWithCreatorIdRequest = PostsWithCreatorIdRequest.newBuilder()
                .setPostsRequest(postsRequest)
                .setCreatorId(ProtoObjectId.newBuilder().setHexString(creatorId.toHexString()).build())
                .build();

        return dispatcher.fetchPostsWithUsernames(postsWithCreatorIdRequest);
    }

    @NonNull
    public List<PostDto> findNextNPostsByCreatorId(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId creatorId,
            @NonNull ScrollPosition scrollPosition
    ) throws UserNotFoundException {

        var postsRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .setPosition(ScrollPositionMapper.map(scrollPosition))
                .build();

        var postsWithCreatorIdRequest = PostsWithCreatorIdRequest.newBuilder()
                .setPostsRequest(postsRequest)
                .setCreatorId(ProtoObjectId.newBuilder().setHexString(creatorId.toHexString()).build())
                .build();

        return dispatcher.fetchPostsWithUsernames(postsWithCreatorIdRequest);
    }

    @NonNull
    public PostWithImageDataDto findByPostId(
            @NonNull ObjectId postId
    ) throws PostNotFoundException, UserNotFoundException {

        var protoPostId = ProtoObjectId.newBuilder().setHexString(postId.toHexString()).build();

        return dispatcher.fetchPostWithImage(protoPostId);
    }

    @NonNull
    public List<PostRatingResponse> findTopNRatings(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId clientId) {

        var postRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .build();

        var ratingsRequest = PostRatingsRequest.newBuilder()
                .setPostsRequest(postRequest)
                .setClientId(ProtoObjectId.newBuilder().setHexString(clientId.toHexString()).build())
                .build();

        return dispatcher.fetchRatings(ratingsRequest);
    }

    @NonNull
    public List<PostRatingResponse> findNextNRatings(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId clientId,
            @NonNull ScrollPosition scrollPosition) {

        var postRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .setPosition(ScrollPositionMapper.map(scrollPosition))
                .build();

        var ratingsRequest = PostRatingsRequest.newBuilder()
                .setPostsRequest(postRequest)
                .setClientId(ProtoObjectId.newBuilder().setHexString(clientId.toHexString()).build())
                .build();

        return dispatcher.fetchRatings(ratingsRequest);
    }

    @NonNull
    public List<PostRatingResponse> findTopNRatingsByCreatorUsername(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId clientId,
            @NonNull String creatorUsername
    ) throws UserNotFoundException {

        var postRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .build();

        var ratingsRequest = PostRatingsRequest.newBuilder()
                .setPostsRequest(postRequest)
                .setClientId(ProtoObjectId.newBuilder().setHexString(clientId.toHexString()).build())
                .build();

        var creatorIdRequest = UserIdRequest.newBuilder().setUsername(creatorUsername).build();

        return dispatcher.fetchRatings(ratingsRequest, creatorIdRequest);
    }

    @NonNull
    public List<PostRatingResponse> findNextNRatingsByCreatorUsername(
            int size,
            @NonNull Collection<Visibility> visibilities,
            @NonNull ObjectId clientId,
            @NonNull ScrollPosition scrollPosition,
            @NonNull String creatorUsername
    ) throws UserNotFoundException {

        var postRequest = PostsRequest.newBuilder()
                .setSize(size)
                .addAllVisibilities(visibilities.stream().map(VisibilityMapper::map).toList())
                .setPosition(ScrollPositionMapper.map(scrollPosition))
                .build();

        var ratingsRequest = PostRatingsRequest.newBuilder()
                .setPostsRequest(postRequest)
                .setClientId(ProtoObjectId.newBuilder().setHexString(clientId.toHexString()).build())
                .build();

        var creatorIdRequest = UserIdRequest.newBuilder().setUsername(creatorUsername).build();

        return dispatcher.fetchRatings(ratingsRequest, creatorIdRequest);
    }

    @NonNull
    public byte[] findImage(
            @NonNull ObjectId postId
    ) throws ImageNotFoundException {

        var protoPostId = ProtoObjectId.newBuilder().setHexString(postId.toHexString()).build();

        return dispatcher.fetchImage(protoPostId);
    }

    public void createPost(
            @NonNull ObjectId clientId,
            @NonNull PostCreationRequest creationRequest,
            @Nullable byte[] imageData
    ) throws FileProcessingException {

        var requestBuilder = CreatePostRequest.newBuilder()
                .setUserId(ProtoObjectId.newBuilder().setHexString(clientId.toHexString()).build())
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
    ) throws PostNotFoundException, RatingNotFoundException {

        var request = RatePostRequest.newBuilder()
                .setPostId(ProtoObjectId.newBuilder().setHexString(postId.toHexString()).build())
                .setUserId(ProtoObjectId.newBuilder().setHexString(clientId.toHexString()).build())
                .setIsPositive(isPositive)
                .build();

        return dispatcher.ratePost(request);
    }

    public int unratePost(
            @NonNull ObjectId postId,
            @NonNull ObjectId clientId
    ) throws PostNotFoundException {

        var request = UnratePostRequest.newBuilder()
                .setPostId(ProtoObjectId.newBuilder().setHexString(postId.toHexString()).build())
                .setUserId(ProtoObjectId.newBuilder().setHexString(clientId.toHexString()).build())
                .build();

        return dispatcher.unratePost(request);
    }

    public void changePostVisibility(
            @NonNull ObjectId postId,
            @NonNull Visibility visibility
    ) throws PostNotFoundException {

        var request = ChangePostVisibilityRequest.newBuilder()
                .setPostId(ProtoObjectId.newBuilder().setHexString(postId.toHexString()).build())
                .setVisibility(VisibilityMapper.map(visibility))
                .build();

        dispatcher.changePostVisibility(request);
    }

    @NonNull
    public ObjectId findPostCreatorId(
            @NonNull ObjectId postId
    ) throws PostNotFoundException {

        var protoPostId = ProtoObjectId.newBuilder().setHexString(postId.toHexString()).build();

        return dispatcher.fetchPostCreatorId(protoPostId);
    }

    @NonNull
    public Role findUserRole(
            @NonNull ObjectId userId
    ) throws UserNotFoundException {

        var protoUserId = ProtoObjectId.newBuilder().setHexString(userId.toHexString()).build();

        return dispatcher.fetchUserRole(protoUserId);
    }

    @NonNull
    public UserDetailsImpl findUserDetails(
            @NonNull String email
    ) throws UserNotFoundException {

        var request = CredentialsRequest.newBuilder()
                .setEmail(email)
                .build();

        return dispatcher.fetchUserCredentials(request);
    }

    public void registerUser(
            @NonNull RegisterRequest registerRequest
    ) throws DuplicateUsernameException, DuplicateEmailException, DuplicateUnexpectedFieldException {

        var requestBuilder = CreateUserRequest.newBuilder()
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

    public void updateUserWithUserPrivilege(
            @NonNull ObjectId userId,
            @NonNull UserUpdateRequestWithUserPrivilege updateRequest
    ) throws UserNotFoundException, DuplicateUsernameException, DuplicateEmailException,
            DuplicateUnexpectedFieldException {

        var optionalRequest = userUpdateMapper.map(userId, updateRequest);

        // if nothing was in the updateRequest, then don't send empty request to microservice
        optionalRequest.ifPresent(dispatcher::updateUser);
    }

    public void updateUserWithAdminPrivilege(
            @NonNull ObjectId userId,
            @NonNull UserUpdateRequestWithAdminPrivilege updateRequest
    ) throws UserNotFoundException, UnsupportedRoleException, DuplicateUsernameException, DuplicateEmailException,
            DuplicateUnexpectedFieldException {

        var optionalRequest = userUpdateMapper.map(userId, updateRequest);

        // if nothing was in the updateRequest, then don't send empty request to microservice
        optionalRequest.ifPresent(dispatcher::updateUser);
    }

    @NonNull
    public Visibility findVisibility(
            @NonNull ObjectId postId
    ) throws UnsupportedVisibilityException, PostNotFoundException {

        return dispatcher.fetchPostVisibility(ProtoObjectId.newBuilder().setHexString(postId.toHexString()).build());
    }

}
