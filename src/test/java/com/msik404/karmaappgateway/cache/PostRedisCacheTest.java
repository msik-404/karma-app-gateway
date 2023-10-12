package com.msik404.karmaappgateway.cache;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msik404.karmaappgateway.RedisConfiguration;
import com.msik404.karmaappgateway.TestingDataGenerator;
import com.msik404.karmaappgateway.dto.PostDto;
import com.msik404.karmaappgateway.dto.Visibility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.NonNull;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = {ObjectMapper.class, RedisConfiguration.class, PostRedisCache.class})
class PostRedisCacheTest {

    private final RedisConnectionFactory redisConnectionFactory;

    private final PostRedisCache redisCache;

    private static final List<PostDto> TEST_CACHED_POSTS = getPostsForTesting();

    @Container
    public static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer(
            DockerImageName.parse("redis:alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @Autowired
    PostRedisCacheTest(RedisConnectionFactory redisConnectionFactory, PostRedisCache redisCache) {

        this.redisConnectionFactory = redisConnectionFactory;
        this.redisCache = redisCache;
    }

    @NonNull
    private static String getPostKey(@NonNull String postId) {
        return String.format("post:%s", postId);
    }

    @NonNull
    private static PostDto getPostDtoForTesting(
            @NonNull long userId,
            @NonNull long postId,
            long karmaScore) {

        final String postIdHexString = TestingDataGenerator.getIdHexString(postId);
        final String userIdHexString = TestingDataGenerator.getIdHexString(userId);

        final String postKey = getPostKey(postIdHexString);

        return new PostDto(
                postIdHexString,
                userIdHexString,
                postKey,
                postKey,
                postKey,
                karmaScore,
                Visibility.ACTIVE
        );
    }

    /**
     * This comparator is made so to mimic redis ordered set reversed range retrieval order.
     */
    static class CachedPostComparator implements Comparator<PostDto> {

        @Override
        public int compare(@NonNull PostDto postOne, @NonNull PostDto postTwo) {

            if (postOne.getKarmaScore().equals(postTwo.getKarmaScore())) {
                final String postKeyOne = getPostKey(postOne.getIdHexString());
                final String postKeyTwo = getPostKey(postTwo.getIdHexString());
                return -postKeyOne.compareTo(postKeyTwo);
            }
            return -postOne.getKarmaScore().compareTo(postTwo.getKarmaScore());
        }

    }

    @NonNull
    private static List<PostDto> getPostsForTesting() {

        final int postsAmount = 9;
        List<PostDto> posts = new ArrayList<>(postsAmount);

        final long userOneId = 1;
        posts.add(getPostDtoForTesting(userOneId, 1, 4));
        posts.add(getPostDtoForTesting(userOneId, 2, -1));
        posts.add(getPostDtoForTesting(userOneId, 3, 5));
        posts.add(getPostDtoForTesting(userOneId, 4, 5));
        posts.add(getPostDtoForTesting(userOneId, 5, 6));

        final long userTwoId = 2;
        posts.add(getPostDtoForTesting(userTwoId, 6, 3));
        posts.add(getPostDtoForTesting(userTwoId, 7, 2));
        posts.add(getPostDtoForTesting(userTwoId, 8, 4));
        posts.add(getPostDtoForTesting(userTwoId, 9, 0));

        posts.sort(new CachedPostComparator());

        return posts;
    }

    @BeforeEach
    void setUp() {
        redisCache.reinitializeCache(TEST_CACHED_POSTS);
    }

    @AfterEach
    void tearDown() {
        redisConnectionFactory.getConnection().serverCommands().flushAll();
    }

    @Test
    void reinitializeCache_TwoPosts_CacheHasOnlyTheseTwoPosts() {

        // given
        final int postsInCacheAmount = 2;
        final List<PostDto> posts = TEST_CACHED_POSTS.subList(0, postsInCacheAmount);

        // when
        redisCache.reinitializeCache(posts);

        // then
        final Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(posts.size());

        assertTrue(optionalCachedPosts.isPresent());

        final List<PostDto> cachedPosts = optionalCachedPosts.get();

        assertEquals(postsInCacheAmount, cachedPosts.size());

        assertEquals(2, cachedPosts.size());

        for (int i = 0; i < posts.size(); i++) {
            assertEquals(posts.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void isEmpty_CacheIsNotEmpty_False() {
        assertFalse(redisCache.isEmpty());
    }

    @Test
    void isEmpty_CacheIsEmpty_True() {

        // given
        redisConnectionFactory.getConnection().serverCommands().flushAll();

        // when
        boolean isCacheEmpty = redisCache.isEmpty();

        // then
        assertTrue(isCacheEmpty);
    }

    @Test
    void findTopNCached_AllCachedPosts_AllCachedPostsFound() {

        // given
        final int size = TEST_CACHED_POSTS.size();

        // when
        final Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(size);

        // then
        assertTrue(optionalCachedPosts.isPresent());

        final List<PostDto> cachedPosts = optionalCachedPosts.get();

        assertEquals(size, cachedPosts.size());

        for (int i = 0; i < cachedPosts.size(); i++) {
            assertEquals(TEST_CACHED_POSTS.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void findNextNCached_NextSizeIsTwoAndTopSizeIsTwo_TwoAfterTopTwoFound() {

        // given
        final int nextSize = 2;
        final int topSize = 2;

        final long lastPostScore = TEST_CACHED_POSTS.get(topSize - 1).getKarmaScore();

        // when
        final Optional<List<PostDto>> optionalNextCachedPosts = redisCache.findNextNCached(nextSize, lastPostScore);

        // then
        assertTrue(optionalNextCachedPosts.isPresent());

        final List<PostDto> nextCachedPosts = optionalNextCachedPosts.get();

        assertEquals(nextSize, nextCachedPosts.size());

        final int endBound = Math.min(topSize + nextSize, TEST_CACHED_POSTS.size());
        List<PostDto> groundTruthNextPosts = TEST_CACHED_POSTS.subList(topSize, endBound);

        for (int i = 0; i < nextCachedPosts.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i), nextCachedPosts.get(i));
        }
    }

    @Test
    void cacheImage_PostIdIsTopAndDataIsTextAsBytes_GetCachedImage() {

        // given
        final PostDto post = TEST_CACHED_POSTS.get(0);
        final byte[] dummyImageData = "imageData".getBytes();

        // when
        assertTrue(redisCache.cacheImage(post.getIdHexString(), dummyImageData));

        final Optional<byte[]> cachedImageData = redisCache.getCachedImage(post.getIdHexString());

        // then
        assertTrue(cachedImageData.isPresent());
        assertArrayEquals(dummyImageData, cachedImageData.get());
    }

    @Test
    void getCachedImage_PostIdIsTopAndDataIsNonExisting_EmptyOptional() {

        // given
        final PostDto post = TEST_CACHED_POSTS.get(0);

        // when
        final Optional<byte[]> cachedImageData = redisCache.getCachedImage(post.getIdHexString());

        // then
        assertFalse(cachedImageData.isPresent());
    }

    @Test
    void updateKarmaScoreIfPresent_PostIdIsTopAndDeltaIsMinusThree_ScoreIsIncreasedAndNewOrderIsInPlace() {

        // given
        final PostDto post = TEST_CACHED_POSTS.get(0);

        final double delta = -3;

        // when
        OptionalDouble newScore = redisCache.updateKarmaScoreIfPresent(post.getIdHexString(), delta);

        // then
        assertTrue(newScore.isPresent());

        assertEquals(post.getKarmaScore() + delta, newScore.getAsDouble());

        final Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(TEST_CACHED_POSTS.size());

        assertTrue(optionalCachedPosts.isPresent());

        final List<PostDto> cachedPosts = optionalCachedPosts.get();

        assertEquals(TEST_CACHED_POSTS.size(), cachedPosts.size());

        final PostDto updatedPost = new PostDto(
                post.getIdHexString(),
                post.getUserIdHexString(),
                post.getUsername(),
                post.getHeadline(),
                post.getText(),
                (long) (post.getKarmaScore() + delta),
                post.getVisibility()
        );

        // shallow copy
        List<PostDto> updatedGroundTruthPosts = new ArrayList<>(TEST_CACHED_POSTS.stream().toList());
        // make the first element reference to updatedPost
        updatedGroundTruthPosts.set(0, updatedPost);
        // sort references
        updatedGroundTruthPosts.sort(new CachedPostComparator());

        for (int i = 0; i < TEST_CACHED_POSTS.size(); i++) {
            assertEquals(updatedGroundTruthPosts.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void updateKarmaScoreIfPresent_PostIdIsNonExistingAndDeltaIsOne_EmptyOptional() {

        // given
        final String nonExistentUserId = TestingDataGenerator.getIdHexString(404);

        final double delta = 1;

        // when
        OptionalDouble newScore = redisCache.updateKarmaScoreIfPresent(nonExistentUserId, delta);

        // then
        assertTrue(newScore.isEmpty());
    }

    @Test
    void deletePostFromCache_PostIdIsTop_PostGotDeletedAndNewOrderIsInPlace() {

        // given
        final PostDto post = TEST_CACHED_POSTS.get(0);

        // when
        boolean wasSuccessful = redisCache.deletePostFromCache(post.getIdHexString());

        // then
        assertTrue(wasSuccessful);

        final int newSize = TEST_CACHED_POSTS.size() - 1;

        final Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(newSize);

        assertTrue(optionalCachedPosts.isPresent());

        List<PostDto> cachedPosts = optionalCachedPosts.get();

        assertEquals(newSize, cachedPosts.size());

        final List<PostDto> groundTruthTopPosts = TEST_CACHED_POSTS.subList(1, TEST_CACHED_POSTS.size());

        for (int i = 0; i < cachedPosts.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void isKarmaScoreGreaterThanLowestScoreInZSet_KarmaScoreIsGreater_OptionalOfTrue() {

        // given
        final long karmaScore = 5;

        // when
        final Optional<Boolean> result = redisCache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void isKarmaScoreGreaterThanLowestScoreInZSet_KarmaScoreIsNotGreater_OptionalOfFalse() {

        // given
        final long karmaScore = -100;

        // when
        final Optional<Boolean> result = redisCache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);

        // then
        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    void isKarmaScoreGreaterThanLowestScoreInZSet_CacheIsEmpty_OptionalEmpty() {

        // given
        redisConnectionFactory.getConnection().serverCommands().flushAll();
        final long karmaScore = 404;

        // when
        final Optional<Boolean> result = redisCache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void insertPost_ImageDataIsNonNullAndThisPostIsNotPresentInCacheNorIsImage_ZSetAndHashShouldBeUpdatedAndImageDataShouldBeCached() {

        // given
        final long userId = 404;
        final long postId = userId;
        final long karmaScore = 5;

        final PostDto postToBeInserted = getPostDtoForTesting(userId, postId, karmaScore);

        final byte[] imageData = "imageData".getBytes();

        List<PostDto> groundTruthPosts = getPostsForTesting();
        groundTruthPosts.add(postToBeInserted);
        groundTruthPosts.sort(new CachedPostComparator());

        // when
        assertTrue(redisCache.insertPost(postToBeInserted, imageData));

        // then

        // ZSet and hash are updated
        final Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(groundTruthPosts.size());
        assertTrue(optionalCachedPosts.isPresent());
        final List<PostDto> cachedPosts = optionalCachedPosts.get();
        assertEquals(groundTruthPosts.size(), cachedPosts.size());

        for (int i = 0; i < groundTruthPosts.size(); i++) {
            assertEquals(groundTruthPosts.get(i), cachedPosts.get(i));
        }

        // image is present in cache
        final Optional<byte[]> optionalCachedImageData = redisCache.getCachedImage(TestingDataGenerator.getIdHexString(postId));
        assertTrue(optionalCachedImageData.isPresent());
        final byte[] cachedImageData = optionalCachedImageData.get();
        assertArrayEquals(imageData, cachedImageData);
    }

    @Test
    void insertPost_ImageDataIsNullAndThisPostIsNotPresentInCacheNorIsImage_ZSetAndHashShouldBeUpdatedAndImageDataShouldNotBeCached() {

        // given
        final long userId = 404;
        final long postId = userId;
        final long karmaScore = 5;

        final PostDto postToBeInserted = getPostDtoForTesting(userId, postId, karmaScore);

        final byte[] imageData = null;

        List<PostDto> groundTruthPosts = getPostsForTesting();
        groundTruthPosts.add(postToBeInserted);
        groundTruthPosts.sort(new CachedPostComparator());

        // when
        assertTrue(redisCache.insertPost(postToBeInserted, imageData));

        // then

        // ZSet and hash are updated
        final Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(groundTruthPosts.size());
        assertTrue(optionalCachedPosts.isPresent());
        final List<PostDto> cachedPosts = optionalCachedPosts.get();
        assertEquals(groundTruthPosts.size(), cachedPosts.size());

        for (int i = 0; i < groundTruthPosts.size(); i++) {
            assertEquals(groundTruthPosts.get(i), cachedPosts.get(i));
        }

        // image is not present in cache
        final Optional<byte[]> optionalCachedImageData = redisCache.getCachedImage(TestingDataGenerator.getIdHexString(postId));
        assertFalse(optionalCachedImageData.isPresent());
    }
}