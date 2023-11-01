package com.msik404.karmaappgateway.post;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.msik404.karmaappgateway.auth.exception.InsufficientRoleException;
import com.msik404.karmaappgateway.post.dto.*;
import com.msik404.karmaappgateway.post.exception.ImageNotFoundException;
import com.msik404.karmaappgateway.post.exception.PostNotFoundException;
import com.msik404.karmaappgateway.post.exception.RatingNotFoundException;
import com.msik404.karmaappgateway.user.exception.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostResponseModelAssembler assembler;

    @GetMapping("guest/posts")
    public List<EntityModel<PostResponse>> findPaginatedPosts(
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam(value = "post_id", required = false) ObjectId postId,
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "username", required = false) String username
    ) throws UserNotFoundException {

        ScrollPosition scrollPosition = null;
        if (postId != null && karmaScore != null) {
            scrollPosition = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPosts(size, List.of(Visibility.ACTIVE), scrollPosition, username)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("user/posts")
    public List<EntityModel<PostResponse>> findPaginatedPosts(
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,
            @RequestParam(value = "post_id", required = false) ObjectId postId,
            @RequestParam(value = "karma_score", required = false) Long karmaScore
    ) throws UserNotFoundException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, false);

        ScrollPosition scrollPosition = null;
        if (postId != null && karmaScore != null) {
            scrollPosition = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedOwnedPosts(size, visibilities, scrollPosition)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("user/posts/ratings")
    public List<PostRatingResponse> findPersonalPostRatings(
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam(value = "post_id", required = false) ObjectId postId,
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "username", required = false) String username
    ) throws UserNotFoundException {


        ScrollPosition scrollPosition = null;
        if (postId != null && karmaScore != null) {
            scrollPosition = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPostRatings(size, List.of(Visibility.ACTIVE), scrollPosition, username);
    }

    @NonNull
    private static List<Visibility> createVisibilityList(boolean active, boolean hidden, boolean deleted) {

        List<Visibility> visibilities = new ArrayList<>();

        if (active) {
            visibilities.add(Visibility.ACTIVE);
        }
        if (hidden) {
            visibilities.add(Visibility.HIDDEN);
        }
        if (deleted) {
            visibilities.add(Visibility.DELETED);
        }
        return visibilities;
    }

    @GetMapping("mod/posts")
    public List<EntityModel<PostResponse>> findPaginatedPosts(
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,
            @RequestParam(value = "post_id", required = false) ObjectId postId,
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "username", required = false) String username
    ) throws UserNotFoundException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, false);

        ScrollPosition scrollPosition = null;
        if (postId != null && karmaScore != null) {
            scrollPosition = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPosts(size, visibilities, scrollPosition, username)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("mod/posts/ratings")
    public List<PostRatingResponse> findPersonalPostRatings(
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,
            @RequestParam(value = "post_id", required = false) ObjectId postId,
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "username", required = false) String username
    ) throws UserNotFoundException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, false);

        ScrollPosition scrollPosition = null;
        if (postId != null && karmaScore != null) {
            scrollPosition = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPostRatings(size, visibilities, scrollPosition, username);
    }

    @GetMapping("admin/posts")
    public List<EntityModel<PostResponse>> findPaginatedPosts(
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,
            @RequestParam(value = "deleted", defaultValue = "false") boolean deleted,
            @RequestParam(value = "post_id", required = false) ObjectId postId,
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "username", required = false) String username
    ) throws UserNotFoundException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, deleted);

        ScrollPosition scrollPosition = null;
        if (postId != null && karmaScore != null) {
            scrollPosition = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPosts(size, visibilities, scrollPosition, username)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("admin/posts/ratings")
    public List<PostRatingResponse> findPersonalPostRatings(
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @RequestParam(value = "hidden", defaultValue = "false") boolean hidden,
            @RequestParam(value = "deleted", defaultValue = "false") boolean deleted,
            @RequestParam(value = "post_id", required = false) ObjectId postId,
            @RequestParam(value = "karma_score", required = false) Long karmaScore,
            @RequestParam(value = "username", required = false) String username
    ) throws UserNotFoundException {

        List<Visibility> visibilities = createVisibilityList(active, hidden, deleted);

        ScrollPosition scrollPosition = null;
        if (postId != null && karmaScore != null) {
            scrollPosition = new ScrollPosition(postId, karmaScore);
        }

        return postService.findPaginatedPostRatings(size, visibilities, scrollPosition, username);
    }

    @GetMapping("guest/posts/{postId}/image")
    public ResponseEntity<byte[]> findImageById(
            @PathVariable ObjectId postId
    ) throws ImageNotFoundException {

        byte[] imageData = postService.findImageByPostId(postId);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    @PostMapping("user/posts")
    public ResponseEntity<Void> create(
            @Valid @RequestPart("json_data") PostCreationRequest jsonData,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        postService.create(jsonData, image);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/rate")
    public ResponseEntity<Void> rate(
            @PathVariable ObjectId postId,
            @RequestParam("is_positive") boolean isPositive
    ) throws PostNotFoundException, RatingNotFoundException {

        postService.rate(postId, isPositive);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/unrate")
    public ResponseEntity<Void> unrate(
            @PathVariable ObjectId postId
    ) throws PostNotFoundException {

        postService.unrate(postId);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/hide")
    public ResponseEntity<Void> hideByUser(
            @PathVariable ObjectId postId
    ) throws InsufficientRoleException, UserNotFoundException, PostNotFoundException {

        postService.changeOwnedPostVisibility(postId, Visibility.HIDDEN);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/unhide")
    public ResponseEntity<Void> unhideByUser(
            @PathVariable ObjectId postId
    ) throws InsufficientRoleException, UserNotFoundException, PostNotFoundException {

        postService.changeOwnedPostVisibility(postId, Visibility.ACTIVE);
        return ResponseEntity.ok(null);
    }

    // todo: this is a little bit stupid because post creator can unhide his post.
    @PostMapping("mod/posts/{postId}/hide")
    public ResponseEntity<Void> hideByMod(
            @PathVariable ObjectId postId
    ) throws InsufficientRoleException, PostNotFoundException {

        postService.changeVisibility(postId, Visibility.HIDDEN);
        return ResponseEntity.ok(null);
    }

    @PostMapping("user/posts/{postId}/delete")
    public ResponseEntity<Void> deleteByUser(
            @PathVariable ObjectId postId
    ) throws InsufficientRoleException, UserNotFoundException, PostNotFoundException {

        postService.changeOwnedPostVisibility(postId, Visibility.DELETED);
        return ResponseEntity.ok(null);
    }

    @PostMapping("admin/posts/{postId}/delete")
    public ResponseEntity<Void> deleteByAdmin(
            @PathVariable ObjectId postId
    ) throws PostNotFoundException {

        postService.changeVisibility(postId, Visibility.DELETED);
        return ResponseEntity.ok(null);
    }

    @PostMapping("admin/posts/{postId}/activate")
    public ResponseEntity<Void> activateByAdmin(
            @PathVariable ObjectId postId
    ) throws PostNotFoundException {

        postService.changeVisibility(postId, Visibility.ACTIVE);
        return ResponseEntity.ok(null);
    }
}
