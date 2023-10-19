package com.msik404.karmaappgateway.grpc.client.zipper;

import java.util.ArrayList;
import java.util.List;

import com.msik404.karmaappgateway.post.dto.PostDto;
import com.msik404.karmaappposts.grpc.Post;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;

public class PostDtoZipper {

    @NonNull
    public static List<PostDto> zipIntoPostsWithUsernames(
            @NonNull List<Post> posts,
            @NonNull List<String> usernames) {

        assert usernames.size() == posts.size();

        List<PostDto> postsWithUsernames = new ArrayList<>(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            postsWithUsernames.add(
                    new PostDto(
                            new ObjectId(post.getPostId().getHexString()),
                            new ObjectId(post.getUserId().getHexString()),
                            usernames.get(i),
                            post.getHeadline(),
                            post.getText(),
                            post.getKarmaScore(),
                            post.getVisibility()
                    )
            );
        }
        return postsWithUsernames;
    }

    @NonNull
    public static List<PostDto> zipIntoPostsWithUsernames(
            @NonNull List<Post> posts,
            @NonNull String username) {

        List<PostDto> postsWithUsernames = new ArrayList<>(posts.size());
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            postsWithUsernames.add(
                    new PostDto(
                            new ObjectId(post.getPostId().getHexString()),
                            new ObjectId(post.getUserId().getHexString()),
                            username,
                            post.getHeadline(),
                            post.getText(),
                            post.getKarmaScore(),
                            post.getVisibility()
                    )
            );
        }
        return postsWithUsernames;

    }
}