package com.msik404.karmaappgateway.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msik404.karmaappgateway.RedisConfiguration;
import com.msik404.karmaappgateway.TestingDataGenerator;
import com.msik404.karmaappgateway.post.cache.PostRedisCache;
import com.msik404.karmaappgateway.post.dto.PostDto;
import org.bson.types.ObjectId;
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

    private static final List<PostDto> TEST_CACHED_POSTS = TestingDataGenerator.getPostsForTesting();
    private static final TestingDataGenerator.CachedPostComparator TEST_COMPARATOR =
            new TestingDataGenerator.CachedPostComparator();

    @Container
    public static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer(
            DockerImageName.parse("redis:alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    private static void registerRedisProperties(@NonNull DynamicPropertyRegistry registry) {

        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @Autowired
    PostRedisCacheTest(RedisConnectionFactory redisConnectionFactory, PostRedisCache redisCache) {

        this.redisConnectionFactory = redisConnectionFactory;
        this.redisCache = redisCache;
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
        int postsInCacheAmount = 2;
        List<PostDto> posts = TEST_CACHED_POSTS.subList(0, postsInCacheAmount);

        // when
        redisCache.reinitializeCache(posts);

        // then
        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(posts.size());

        assertTrue(optionalCachedPosts.isPresent());

        List<PostDto> cachedPosts = optionalCachedPosts.get();

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
        int size = TEST_CACHED_POSTS.size();

        // when
        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(size);

        // then
        assertTrue(optionalCachedPosts.isPresent());

        List<PostDto> cachedPosts = optionalCachedPosts.get();

        assertEquals(size, cachedPosts.size());

        for (int i = 0; i < cachedPosts.size(); i++) {
            assertEquals(TEST_CACHED_POSTS.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void findNextNCached_NextSizeIsTwoAndTopSizeIsTwo_TwoAfterTopTwoFound() {

        // given
        int nextSize = 2;
        int topSize = 2;

        long lastPostScore = TEST_CACHED_POSTS.get(topSize - 1).getKarmaScore();

        // when
        Optional<List<PostDto>> optionalNextCachedPosts = redisCache.findNextNCached(nextSize, lastPostScore);

        // then
        assertTrue(optionalNextCachedPosts.isPresent());

        List<PostDto> nextCachedPosts = optionalNextCachedPosts.get();

        assertEquals(nextSize, nextCachedPosts.size());

        int endBound = Math.min(topSize + nextSize, TEST_CACHED_POSTS.size());
        List<PostDto> groundTruthNextPosts = TEST_CACHED_POSTS.subList(topSize, endBound);

        for (int i = 0; i < nextCachedPosts.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i), nextCachedPosts.get(i));
        }
    }

    @Test
    void cacheImage_PostIdIsTopAndDataIsTextAsBytes_GetCachedImage() {

        // given
        PostDto post = TEST_CACHED_POSTS.get(0);
        byte[] dummyImageData = "imageData".getBytes();

        // when
        assertTrue(redisCache.cacheImage(post.getId(), dummyImageData));

        Optional<byte[]> cachedImageData = redisCache.getCachedImage(post.getId());

        // then
        assertTrue(cachedImageData.isPresent());
        assertArrayEquals(dummyImageData, cachedImageData.get());
    }

    @Test
    void getCachedImage_PostIdIsTopAndDataIsNonExisting_EmptyOptional() {

        // given
        PostDto post = TEST_CACHED_POSTS.get(0);

        // when
        Optional<byte[]> cachedImageData = redisCache.getCachedImage(post.getId());

        // then
        assertFalse(cachedImageData.isPresent());
    }

    @Test
    void updateKarmaScoreIfPresent_PostIdIsTopAndDeltaIsMinusThree_ScoreIsIncreasedAndNewOrderIsInPlace() {

        // given
        PostDto post = TEST_CACHED_POSTS.get(0);

        double delta = -3;

        // when
        OptionalDouble newScore = redisCache.updateKarmaScoreIfPresent(post.getId(), delta);

        // then
        assertTrue(newScore.isPresent());

        assertEquals(post.getKarmaScore() + delta, newScore.getAsDouble());

        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(TEST_CACHED_POSTS.size());

        assertTrue(optionalCachedPosts.isPresent());

        List<PostDto> cachedPosts = optionalCachedPosts.get();

        assertEquals(TEST_CACHED_POSTS.size(), cachedPosts.size());

        PostDto updatedPost = new PostDto(
                post.getId(),
                post.getUserId(),
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
        updatedGroundTruthPosts.sort(TEST_COMPARATOR);

        for (int i = 0; i < TEST_CACHED_POSTS.size(); i++) {
            assertEquals(updatedGroundTruthPosts.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void updateKarmaScoreIfPresent_PostIdIsNonExistingAndDeltaIsOne_EmptyOptional() {

        // given
        ObjectId nonExistentUserId = TestingDataGenerator.getId(404);

        double delta = 1;

        // when
        OptionalDouble newScore = redisCache.updateKarmaScoreIfPresent(nonExistentUserId, delta);

        // then
        assertTrue(newScore.isEmpty());
    }

    @Test
    void deletePostFromCache_PostIdIsTop_PostGotDeletedAndNewOrderIsInPlace() {

        // given
        PostDto post = TEST_CACHED_POSTS.get(0);

        // when
        boolean wasSuccessful = redisCache.deletePostFromCache(post.getId());

        // then
        assertTrue(wasSuccessful);

        int newSize = TEST_CACHED_POSTS.size() - 1;

        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(newSize);

        assertTrue(optionalCachedPosts.isPresent());

        List<PostDto> cachedPosts = optionalCachedPosts.get();

        assertEquals(newSize, cachedPosts.size());

        List<PostDto> groundTruthTopPosts = TEST_CACHED_POSTS.subList(1, TEST_CACHED_POSTS.size());

        for (int i = 0; i < cachedPosts.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i), cachedPosts.get(i));
        }
    }

    @Test
    void isKarmaScoreGreaterThanLowestScoreInZSet_KarmaScoreIsGreater_OptionalOfTrue() {

        // given
        long karmaScore = 5;

        // when
        Optional<Boolean> result = redisCache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void isKarmaScoreGreaterThanLowestScoreInZSet_KarmaScoreIsNotGreater_OptionalOfFalse() {

        // given
        long karmaScore = -100;

        // when
        Optional<Boolean> result = redisCache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);

        // then
        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    void isKarmaScoreGreaterThanLowestScoreInZSet_CacheIsEmpty_OptionalEmpty() {

        // given
        redisConnectionFactory.getConnection().serverCommands().flushAll();
        long karmaScore = 404;

        // when
        Optional<Boolean> result = redisCache.isKarmaScoreGreaterThanLowestScoreInZSet(karmaScore);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void insertPost_ImageDataIsNonNullAndThisPostIsNotPresentInCacheNorIsImage_ZSetAndHashShouldBeUpdatedAndImageDataShouldBeCached() {

        // given
        long userId = 404;
        long postId = userId;
        long karmaScore = 5;

        PostDto postToBeInserted = TestingDataGenerator.getPostDtoForTesting(userId, postId, karmaScore);

        byte[] imageData = "imageData".getBytes();

        List<PostDto> groundTruthPosts = TestingDataGenerator.getPostsForTesting();
        groundTruthPosts.add(postToBeInserted);
        groundTruthPosts.sort(new TestingDataGenerator.CachedPostComparator());

        // when
        assertTrue(redisCache.insertPost(postToBeInserted, imageData));

        // then

        // ZSet and hash are updated
        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(groundTruthPosts.size());
        assertTrue(optionalCachedPosts.isPresent());
        List<PostDto> cachedPosts = optionalCachedPosts.get();
        assertEquals(groundTruthPosts.size(), cachedPosts.size());

        for (int i = 0; i < groundTruthPosts.size(); i++) {
            assertEquals(groundTruthPosts.get(i), cachedPosts.get(i));
        }

        // image is present in cache
        Optional<byte[]> optionalCachedImageData = redisCache.getCachedImage(
                TestingDataGenerator.getId(postId));

        assertTrue(optionalCachedImageData.isPresent());
        byte[] cachedImageData = optionalCachedImageData.get();
        assertArrayEquals(imageData, cachedImageData);
    }

    @Test
    void insertPost_ImageDataIsNullAndThisPostIsNotPresentInCacheNorIsImage_ZSetAndHashShouldBeUpdatedAndImageDataShouldNotBeCached() {

        // given
        long userId = 404;
        long postId = userId;
        long karmaScore = 5;

        PostDto postToBeInserted = TestingDataGenerator.getPostDtoForTesting(userId, postId, karmaScore);

        byte[] imageData = null;

        List<PostDto> groundTruthPosts = TestingDataGenerator.getPostsForTesting();
        groundTruthPosts.add(postToBeInserted);
        groundTruthPosts.sort(TEST_COMPARATOR);

        // when
        assertTrue(redisCache.insertPost(postToBeInserted, imageData));

        // then

        // ZSet and hash are updated
        Optional<List<PostDto>> optionalCachedPosts = redisCache.findTopNCached(groundTruthPosts.size());
        assertTrue(optionalCachedPosts.isPresent());
        List<PostDto> cachedPosts = optionalCachedPosts.get();
        assertEquals(groundTruthPosts.size(), cachedPosts.size());

        for (int i = 0; i < groundTruthPosts.size(); i++) {
            assertEquals(groundTruthPosts.get(i), cachedPosts.get(i));
        }

        // image is not present in cache
        Optional<byte[]> optionalCachedImageData = redisCache.getCachedImage(
                TestingDataGenerator.getId(postId));

        assertFalse(optionalCachedImageData.isPresent());
    }
}