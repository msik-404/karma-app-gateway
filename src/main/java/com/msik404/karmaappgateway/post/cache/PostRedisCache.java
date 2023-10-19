package com.msik404.karmaappgateway.post.cache;

import java.time.Duration;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msik404.karmaappgateway.post.dto.PostDto;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.redis.connection.DefaultStringTuple;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostRedisCache {

    private static final String KARMA_SCORE_ZSET_KEY = "karma-score-zset";
    private static final String POST_HASH_KEY = "posts-hash";
    private static final String POST_PREFIX = "post";

    private static final Duration TIMEOUT = Duration.ofSeconds(3600);

    @NonNull
    private static String getPostKey(@NonNull ObjectId postId) {
        return String.format("%s:%s", POST_PREFIX, postId.toHexString());
    }

    @NonNull
    private static String getPostImageKey(@NonNull ObjectId postId) {
        return getPostKey(postId) + ":image";
    }

    private final ObjectMapper objectMapper;

    private final StringRedisTemplate redisTemplate;

    /**
     * Method caches posts in redis. It uses ZSet with key: KARMA_SCORE_ZSET_KEY for keeping the order of post
     * for data retrieval. Posts as stored in Hash with key: POST_HASH_KEY in a form of string key, value pairs,
     * values are serialized as json strings. Values are first computed to a format compatible with redis pipeline
     * API, and then pipelined for maximum performance.
     *
     * @param posts Collection of posts which should be placed in a cache.
     */
    public void reinitializeCache(@NonNull Collection<PostDto> posts) {

        Set<StringRedisConnection.StringTuple> tuplesToAdd = new HashSet<>(posts.size());
        for (PostDto post : posts) {
            var tuple = new DefaultStringTuple(getPostKey(post.getId()), (double) post.getKarmaScore());
            tuplesToAdd.add(tuple);
        }

        Map<String, String> valuesMap = new HashMap<>(posts.size());
        for (PostDto post : posts) {
            valuesMap.put(getPostKey(post.getId()), serialize(post));
        }

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {

            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

            stringRedisConn.del(KARMA_SCORE_ZSET_KEY);
            stringRedisConn.del(POST_HASH_KEY);

            stringRedisConn.zAdd(KARMA_SCORE_ZSET_KEY, tuplesToAdd);
            stringRedisConn.expire(KARMA_SCORE_ZSET_KEY, TIMEOUT.getSeconds());

            stringRedisConn.hMSet(POST_HASH_KEY, valuesMap);
            stringRedisConn.expire(POST_HASH_KEY, TIMEOUT.getSeconds());

            return null;
        });
    }

    /**
     * @return true if both zSet with post scores and hash with post contents are present in cache else false
     */
    public boolean isEmpty() {

        final List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

            stringRedisConn.exists(KARMA_SCORE_ZSET_KEY);
            stringRedisConn.exists(POST_HASH_KEY);

            return null;
        });

        return results.size() == 2 && !(Boolean) results.get(0) && !(Boolean) results.get(1);
    }

    public boolean cacheImage(@NonNull ObjectId postId, byte[] imageData) {

        Object results = redisTemplate.execute((RedisCallback<Object>) connection ->
                connection.stringCommands().set(
                        getPostImageKey(postId).getBytes(),
                        imageData,
                        Expiration.from(TIMEOUT),
                        RedisStringCommands.SetOption.ifAbsent())
        );

        return Boolean.TRUE.equals(results);
    }

    @NonNull
    public Optional<byte[]> getCachedImage(@NonNull ObjectId postId) {

        Object results = redisTemplate.execute((RedisCallback<Object>) connection ->
                connection.stringCommands().getEx(
                        getPostImageKey(postId).getBytes(),
                        Expiration.from(TIMEOUT))
        );

        return Optional.ofNullable((byte[]) results);
    }

    @NonNull
    private List<PostDto> findCachedByZSet(
            @NonNull Collection<ZSetOperations.TypedTuple<String>> postIdKeySetWithScores) {

        final int size = postIdKeySetWithScores.size();

        List<String> postIdKeyList = new ArrayList<>(size);
        List<Double> postScoreList = new ArrayList<>(size);
        for (ZSetOperations.TypedTuple<String> tuple : postIdKeySetWithScores) {
            postIdKeyList.add(Objects.requireNonNull(tuple.getValue()));
            postScoreList.add(tuple.getScore());
        }

        final HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        final List<String> serializedResults = hashOps.multiGet(POST_HASH_KEY, postIdKeyList);

        List<PostDto> results = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            PostDto postDto = deserialize(serializedResults.get(i));
            postDto.setKarmaScore(postScoreList.get(i).longValue());
            results.add(postDto);
        }

        return results;
    }

    @NonNull
    public Optional<List<PostDto>> findTopNCached(int size) {

        final Set<ZSetOperations.TypedTuple<String>> postIdKeySetWithScores = redisTemplate.opsForZSet()
                .reverseRangeWithScores(KARMA_SCORE_ZSET_KEY, 0, size - 1);

        if (postIdKeySetWithScores == null || postIdKeySetWithScores.size() != size) {
            return Optional.empty();
        }

        return Optional.of(findCachedByZSet(postIdKeySetWithScores));
    }

    @NonNull
    public Optional<List<PostDto>> findNextNCached(int size, long karmaScore) {

        // offset is one because we have to skip first element with karmaScore, otherwise we will have duplicates
        // in pagination
        final Set<ZSetOperations.TypedTuple<String>> postIdKeySetWithScores = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(
                        KARMA_SCORE_ZSET_KEY, Double.NEGATIVE_INFINITY, karmaScore, 1, size);

        if (postIdKeySetWithScores == null || postIdKeySetWithScores.size() != size) {
            return Optional.empty();
        }

        return Optional.of(findCachedByZSet(postIdKeySetWithScores));
    }

    @NonNull
    public OptionalDouble updateKarmaScoreIfPresent(@NonNull ObjectId postId, double delta) {

        final ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        final String postIdKey = getPostKey(postId);

        if (zSetOps.score(KARMA_SCORE_ZSET_KEY, postIdKey) == null) {
            return OptionalDouble.empty();
        }

        final Double newScore = zSetOps.incrementScore(KARMA_SCORE_ZSET_KEY, postIdKey, delta);

        if (newScore == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(newScore);
    }

    public boolean deletePostFromCache(@NonNull ObjectId postId) {

        final String postIdKey = getPostKey(postId);

        final List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;

            stringRedisConn.zRem(KARMA_SCORE_ZSET_KEY, postIdKey);
            stringRedisConn.hDel(POST_HASH_KEY, postIdKey);
            stringRedisConn.del(getPostImageKey(postId));

            return null;
        });

        return results.size() == 3 && (Long) results.get(0) == 1 && (Long) results.get(1) == 1;
    }

    @NonNull
    public Optional<Boolean> isKarmaScoreGreaterThanLowestScoreInZSet(long karmaScore) {

        final Set<ZSetOperations.TypedTuple<String>> lowestScorePostIdWithScore = redisTemplate.opsForZSet()
                .rangeWithScores(KARMA_SCORE_ZSET_KEY, 0, 0);

        if (lowestScorePostIdWithScore == null || lowestScorePostIdWithScore.size() != 1) {
            return Optional.empty();
        }

        final Double lowestScore = lowestScorePostIdWithScore.iterator().next().getScore();
        if (lowestScore == null) {
            return Optional.empty();
        }

        return Optional.of(lowestScore < karmaScore);
    }

    public boolean insertPost(@NonNull PostDto post, @Nullable byte[] imageData) {

        final String serializedPost = serialize(post);

        final List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {

            final byte[] postKeyBytes = getPostKey(post.getId()).getBytes();

            connection.zSetCommands().zAdd(
                    KARMA_SCORE_ZSET_KEY.getBytes(),
                    (double) post.getKarmaScore(),
                    postKeyBytes,
                    RedisZSetCommands.ZAddArgs.empty()
            );

            connection.hashCommands().hSet(
                    POST_HASH_KEY.getBytes(),
                    postKeyBytes,
                    serializedPost.getBytes()
            );

            if (imageData != null) {
                connection.stringCommands().set(
                        getPostImageKey(post.getId()).getBytes(),
                        imageData,
                        Expiration.from(TIMEOUT),
                        RedisStringCommands.SetOption.ifAbsent()
                );
            }

            return null;
        });

        if (imageData == null) {
            return results.size() == 2 && (Boolean) results.get(0) && (Boolean) results.get(1);
        }
        return results.size() == 3 && (Boolean) results.get(0) && (Boolean) results.get(1) && (Boolean) results.get(2);
    }

    @NonNull
    private String serialize(@NonNull PostDto post) {

        try {
            return objectMapper.writeValueAsString(post);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing PostJoinedDto to JSON", e);
        }
    }

    @NonNull
    private PostDto deserialize(@NonNull String json) {

        try {
            return objectMapper.readValue(json, PostDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing JSON to PostJoinedDto", e);
        }
    }

}
