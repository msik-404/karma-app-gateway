package com.msik404.karmaappgateway.grpc.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.msik404.karmaappgateway.dto.PostDto;
import com.msik404.karmaappgateway.grpc.client.exception.InternalServerErrorException;
import com.msik404.karmaappgateway.grpc.client.mapper.MongoObjectIdMapper;
import com.msik404.karmaappposts.grpc.Post;
import com.msik404.karmaappposts.grpc.PostsGrpc;
import com.msik404.karmaappposts.grpc.PostsRequest;
import com.msik404.karmaappposts.grpc.PostsResponse;
import com.msik404.karmaappusers.grpc.MongoObjectId;
import com.msik404.karmaappusers.grpc.UsernamesRequest;
import com.msik404.karmaappusers.grpc.UsernamesResponse;
import com.msik404.karmaappusers.grpc.UsersGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GrpcHandler {

    private final PostsGrpc.PostsFutureStub postsStub;
    private final UsersGrpc.UsersFutureStub usersStub;

    @NonNull
    public List<PostDto> fetchPostsWithUsernames(
            @NonNull PostsRequest postsRequest) throws InternalServerErrorException {

        try {
            final PostsResponse postsResponse = postsStub.findPosts(postsRequest).get();
            final List<Post> posts = postsResponse.getPostsList();

            final List<MongoObjectId> userIds = posts.stream()
                    // this map is giga stupid, because both proto files redefine the same MongoObjectId message.
                    .map(post -> MongoObjectIdMapper.mapToUsersMongoObjectId(post.getPostId().getHexString()))
                    .toList();

            final var usernamesRequest = UsernamesRequest.newBuilder().addAllUserIds(userIds).build();
            final UsernamesResponse usernamesResponse = usersStub.findUsernames(usernamesRequest).get();

            return zipIntoPostsWithUsernames(usernamesResponse, posts);

        } catch (InterruptedException | ExecutionException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        }
    }

    @NonNull
    private static List<PostDto> zipIntoPostsWithUsernames(
            @NonNull UsernamesResponse usernamesResponse,
            @NonNull List<Post> posts) {

        final List<String> usernames = usernamesResponse.getUsernamesList();

        assert usernames.size() == posts.size();

        final List<PostDto> newValuesForCache = new ArrayList<>(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            final Post post = posts.get(i);
            newValuesForCache.add(
                    new PostDto(
                            post.getPostId().getHexString(),
                            post.getUserId().getHexString(),
                            usernames.get(i),
                            post.getHeadline(),
                            post.getText(),
                            post.getKarmaScore(),
                            post.getVisibility()
                    )
            );
        }
        return newValuesForCache;
    }

}
