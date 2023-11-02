# karma-app-gateway
Gateway microservice for karma-app microservices version. karma-app-gateway is http server which exposes rest endpoints 
and communicate with other microservices to perform operations. karma-app-gateway support caching by using redis.

# Technologies used
- Java 21
- Redis
- Docker
- GRPC
- Java spring
- spring-boot-starter-test
- [spring-boot-testcontainers](https://spring.io/blog/2023/06/23/improved-testcontainers-support-in-spring-boot-3-1)
- junit-jupiter
- [grpc-java](https://github.com/grpc/grpc-java)
- [protovalidate-java](https://github.com/bufbuild/protovalidate-java)
- lombok

# Grpc, Protobuf and protovalidate
Thanks to the use of grpc all available service methods and messages can be
inspected inside [proto file](https://github.com/msik-404/karma-app-posts/blob/main/src/main/proto/karma_app_posts.proto).

In this project to help with message validation I use: [protovalidate-java](https://github.com/bufbuild/protovalidate-java).
This project significantly simplifies validation of messages and reduces the time required to build stable system.
Additionally potential user of this microservice can see which fields are required and what
constraints need to be met to build valid message.

# Features

### Rest endpoints
All endpoints reside in [PostController](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/PostController.java),
[UserController](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/user/UserController.java)
and [AuthController](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/auth/AuthController.java)

These are all the supported endpoints.

#### [PostController.java](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/PostController.java)

```
GET /guest/posts?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL=username=OPTIONAL
```
This endpoint is available for all users, that is: not logged-in users, logged-in users, mods and admins. It is used
for getting posts, size is the amount of posts to get, karma_score and post_id is used for pagination. Simply set them 
to values of last previously fetched post, and you will get size amount of next posts. Argument username is used for
getting only posts created by user with that username. If username is left out, cache can be used.

Response json:
```
[
    {
        "id": "SOME_24_CHAR_HEX_STRING", # this is always set
        "username": "USERNAME",          # this is always set
        "headline": "HEADLINE",          # this might not be set
        "text": "TEXT",                  # this might not be set
        "karmascore": "42",              # this is always set
        "visibility": "ACTIVE",          # this is always set
        "links": [                       # these endpoints support HATEOAS
            {
                "rel": "self",
                "href": "ENDPOINT FOR GETTING IMAGE"
            },
            {
                "rel": "self",
                "href": "ENDPOINT FOR RATING POSITIVELY"
            },
            ...                          # and so on other links
        ]
    },
...                                      # and so on other size - 1 posts
]
```

For testing purposes I made that each post has not only link for image but also links for all endpoints which can change
it state. That is: rate positively, rate negatively, unrate, change visibility by user, change visibility by mod and admin.
In real case scenario I would only leave link for image, to reduce overhead.

```
GET /user/posts?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL&active=OPTIONAL_BOOL&hidden=OPTIONAL_BOOL
```
This endpoint is similar to guest one, but returns only posts created by logged-in user, therefore it requires user to
be logged-in. Arguments active and hidden can be set to true, if so both active and hidden posts are displayed. Of course
user can also set only active=true to see only active posts or only hidden=true to see only hidden posts. If bot active
and hidden are not set, by default only active posts are returned. This endpoint does not use cache.

```
GET /user/posts/ratings?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_scored=OPTIONAL&username=OPTIONAL
```
This endpoint is used for getting information on how logged-in user rated posts. This works in the same way as guest/posts
and user/posts but for each post from these endpoints, it returns information on how logged-in user rated these posts.
Null if given post was not rated, true if was rated positively and false if was rated negatively. The purpose of this 
endpoint is solely for the purpose of frontend, so that the client can see if he has already rated something and if so how.
I made this endpoint independent so that caching of top posts could be made. This endpoint does not use cache. 
Frontend developer should make two async calls to render front page. First call to guest/posts (this should be shown as
fast as possible), second call to user/posts/ratings (if user is logged-in) once the response data comes, this information
should be somehow displayed to the client.

Response json:
```
[
    {
        "id": "SOME_24_CHAR_HEX_STRING", # this is always set
        "wasRatedPositively": null       # this is either null, false or true
    },
    ...                                  # and so on other size - 1 post ratings
]

```

```
GET /mod/posts?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL=username=OPTIONAL&active=OPTIONAL_BOOL&hidden=OPTIONAL_BOOL
```
This endpoint works similar to `guest/posts` but mod user can also see hidden posts. User is required to be logged-in and
have mod or admin role.

```
GET /mod/posts/ratings?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL=username=OPTIONAL&active=OPTIONAL_BOOL&hidden=OPTIONAL_BOOL
```
This endpoint works similar to `user/posts/ratings` but mod user can also see hidden posts. User is required to be 
logged-in and have mod or admin role.

```
GET /admin/posts?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL=username=OPTIONAL&active=OPTIONAL_BOOL&hidden=OPTIONAL_BOOL&deleted=OPTIONAL_BOOL
```
This endpoint works similar to `mod/posts` but admin user can also see deleted posts. User is required to be logged-in and
have admin role.

```
GET /admin/posts/ratings?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL=username=OPTIONAL&active=OPTIONAL_BOOL&hidden=OPTIONAL_BOOL&deleted=OPTIONAL_BOOL
```
This endpoint works similar to `mod/posts/ratings` but admin user can also see deleted posts. User is required to be
logged-in and have admin role.

```
GET /guests/posts/{postId}/image # postId is SOME_24_CHAR_HEX_STRING
```
This endpoint is used for getting image of a given post. If post is not found HTTP error 404 with appropriate message is
returned. Image is returned as byte array with media type IMAGE_JPEG.

```
POST /user/posts
```
This endpoint is used for creating posts. User needs to be logged-in to use it. It uses `multipart/form-data` 
because image data is binary.

The first part of the request is `Key: json_data` and `Value: JSON_BODY` with content type `application/json`.

JSON BODY:
```
{
    "text": "SOME_POST_TEXT",
    "headline": "SOME_POST_HEADLINE"
}
```
Both text and headline are optional, but json_data key must be set.

The second part of the request is `Key: image` and `Value: BINARY_IMAGE_DATA` with HTTP `<input type="file">`.

```
POST /user/posts/{postId}/rate?is_positive=TRUE|FALSE
```
This endpoint is used for rating posts. User needs to be logged-in to use it. Argument is_positive is required. 
If post is not found `HTTP status code 404 Not Found` response with appropriate message is returned.
If post gets rated by the same user, second time the same way, nothing will happen and user will get `HTTP status code 200 OK` 
response. Theoretically Rating not found exception could be thrown but it shouldn't happen because rating operation takes 
place in transaction. This endpoint uses cache. Post will get cached if one of these two things take place at the time of 
rating the post: 
- first: cache is not yet full.
- second: post karma score after rating is higher than the lowest score of a post in cache.

```
POST /user/posts/{postId}/unrate
```
This endpoint is used for unrating. User needs to be logged-in to use it. If post is not found `HTTP status code 404 Not Found` 
with appropriate message is returned. If post was not rated by the user, nothing happens and client gets 
`HTTP status code 200 OK` response. This endpoint uses cache. Post will get cached if one 
of these two things take place at the time of rating the post:
- first: cache is not yet full.
- second: post karma score after unrating is higher than the lowest score of a post in cache.

```
POST /user/posts/{postId}/hide
```
This endpoint is used for hiding owned posts. User needs to be logged-in to use it and be its creator.
If post is not found `HTTP status code 404 Not Found` response is returned with appropriate 
message. If user is not the owner `HTTP status code 401 Unauthorized` response is returned. This exception is also returned when 
posts visibility is set to deleted(only admin can change deleted visibility). This endpoint uses cache. If post before 
this operation was cached, after this operation it will get evicted from cache. Because underlying method can also be 
used for changing visibility to active, underneath entire post fetch takes place, which might throw user not found 
exception, because cached posts need to have username, and username needs to be fetched by userId. Practically this 
exception will never be thrown.

```
POST /user/posts/{postId}/unhide
```
This endpoint is almost identical to `user/posts/{postId}/hide` the only difference is that it changes visibility to 
active (if it was not set to deleted prior).

```
POST /user/posts/{postId}/delete
```
This endpoint is almost identical to `user/posts/{postId}/hide` the only difference is that it changes visibility to
deleted.

```
POST /mod/posts/{postId}/hide
```
This endpoint enables mod to hide every active post. If post is not found `HTTP status code 404 Not Found` response is returned 
with appropriate message. If post visibility was set to deleted `HTTP status code 401 Unauthorized` response with appropriate 
message is returned. This endpoint uses cache. If post before this operation was cached, after this operation it will 
get evicted from cache.

```
POST /admin/posts/{postId}/delete
```
This endpoint is similar to `mod/posts/{postId}/hide` the only difference is that it changes visibility to deleted and 
requires the user to be admin.

```
POST /admin/posts/{postId}/activate
```
This endpoint is similar to `admin/posts/{postId}/delete` the only difference is that it changes visibility to active.
Post will get cached if one of these two things take place at the time of activating:
- first: cache is not yet full.
- second: post karma score after unrating is higher than the lowest score of a post in cache.

#### [UserController.java](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/user/UserController.java)

```
PUT /user/users
```
This endpoint is used for updating currency logged-in user while having user privilege. All fields are optional, 
only non-empty fields will be updated. If provided username or email are duplicates, `HTTP status code 409 CONFLICT` response 
with appropriate message is returned.

Request json:
```
{
    "firstName": "FIRSTNAME", # this is optional
    "lastName": "LASTNAME",   # this is optional
    "username": "USERNAME"    # this is optional, but must be unique
    "email": "EMAIL",         # this is optional, but must be unique and valid email
    "password": "PASSWORD"    # this is optional
}
```

```
PUT /admin/users/{userId} # userId is SOME_24_CHAR_HEX_STRING
```
This endpoint is similar to `user/users` but it enables user with admin privilege to update any user. Additionally, it
enables admin to change role(privilege) of other users.

Request json:
```
{
    "firstName": "FIRSTNAME", # this is optional
    "lastName": "LASTNAME",   # this is optional
    "username": "USERNAME"    # this is optional, but must be unique
    "email": "EMAIL",         # this is optional, but must be unique and valid email
    "password": "PASSWORD"    # this is optional
    "role": "USER|MOD|ADMIN"  # this is optional, but must be one of these three values
}
```

#### [AuthController.java](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/auth/AuthController.java)
```
POST /register
```
This endpoint is used for creating new user. If provided username or email are duplicates, `HTTP status code 409 CONFLICT` response
with appropriate message is returned.

Request json:
```
{
    "firstName": "FIRSTNAME", # this is optional
    "lastName": "LASTNAME",   # this is optional
    "username": "USERNAME"    # this is required, but must be unique
    "email": "EMAIL",         # this is required, but must be unique and valid email
    "password": "PASSWORD"    # this is required 
}
```

### How to access non-guest endpoints

```
POST /login
```
This endpoint is used for acquiring `JWT_STRING`. This JWT is signed with HMAC-SHA256 using KARMA_APP_GATEWAY_SECRET 
environment variable secret. 

JWT has two claims set:
- sub (subject) to logged-in user id which is SOME_24_CHAR_HEX_STRING.
- exp (expiration time) to one hour.

Client to use any non-guest endpoint should set `HTTP Authorization header` to `Berear JWT_STRING`.

JWT is being validated on each endpoint use. If validation fails`HTTP status code 401 Unauthorized` response with 
appropriate message is returned.

Request json:
```
{
    "email": "EMAIL",         # this is required, and must be valid email
    "password": "PASSWORD"    # this is required 
}
```

If user fails to log-in `HTTP status code 401 Unauthorized` response with appropriate message is returned.

### Exception encoding
When some exception which is not critical is thrown on the backend side, it is being encoded and passed with appropriate
grpc code to the caller. Each exception has its unique identifier. With this it can be decoded on the caller side.
In this setup client side can use the same exception classes as backend.

Simple [encoding class](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/grpc/client/encoding/ExceptionEncoder.java)
which simply inserts "exceptionId EXCEPTION_ID" at the begging of error message. This EXCEPTION_ID can be parsed with
simple regex.

Each encodable exception must implement [EncodableException](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/grpc/client/encoding/EncodableException.java)
and [GrpcStatusException](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/grpc/client/exception/GrpcStatusException.java).

karma-app-gateway has [decoding class](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/grpc/client/encoding/ExceptionDecoder.java)
implemented, which takes encoded message and returns appropriate exception.

### Cache
As one could notice from endpoint documentation this microservice uses caching. Cache can be used for fetching any subset
of [MAX_CACHED_POSTS](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/cache/PostRedisCache.java#L37) 
posts as long as no filtering rules are set (filter by username or visibility other 
than active). Each post state change which is persisted in database is being reflected to the cache. 
That is post score and visibility changes. Rating posts high enough might make them present in cache. Making post
visibility hidden or deleted evicts it from cache if it was cached prior. Every post get cached if max cached posts count
is not yet reached.

#### How is it implemented?

I use several redis structures for this:

- [Redis sorted sets](https://redis.io/docs/data-types/sorted-sets/) (ZSet) for preserving top posts rating (all cached posts). ZSet is set under the [KARMA_SCORE_ZSET_KEY](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/cache/PostRedisCache.java#L28).
ZSet contains Keys in [post_key](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/cache/PostRedisCache.java#L44) 
format, each post_key has score which is post karmaScore. Score is being updated in real time, so that post score does not become stale.
KARMA_SCORE_ZSET_KEY expires after [TIMEOUT](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/cache/PostRedisCache.java#L32).

- [Redis hashes](https://redis.io/docs/data-types/hashes/) for storing all post non-image data. Each field is post_key 
and value is json serialized [PostDto.java](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/dto/PostDto.java).
There are as many fields as there are keys in ZSet.
This hash is set under the [POST_HASH_KEY](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/cache/PostRedisCache.java#L29),
it expires after TIMEOUT.

- [Redis Strings](https://redis.io/docs/data-types/strings/) are used for storing image data. Each image binary data is found under the [post_image_key](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/cache/PostRedisCache.java#L49).
Each [post_image_key](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/cache/PostRedisCache.java#L49) 
has expiration time set to TIMEOUT which is one hour. [post_image_key](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/cache/PostRedisCache.java#L49) 
is set once the image is requested for the first time. Expiration time is reset each 
time the data is requested within TIMEOUT.

I used this [redis.conf](https://github.com/msik-404/karma-app-gateway/blob/main/redis.conf). The most important things
about it are that is uses: [AOF and RDB](https://redis.io/docs/management/persistence/).

My cache code uses [Redis pipelining](https://redis.io/docs/manual/pipelining/) when more than single operation needs to 
be preformed. This improves efficiency, by reducing required number of request.

Because ZSet [ZRANGE](https://redis.io/commands/zrange/) cannot be trivially used for getting key-set paginated values I
had to come up with a solution. If reader is interested in details look inside [findNextNCached](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/cache/PostRedisCache.java#L195)
method code and comments.

#### Note
Maximum amount of posts cached can exceed [MAX_CACHED_POSTS](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/cache/PostRedisCache.java#L37)
this is because of the second rule for caching during rating posts positively:

```
Post will get cached if one of these two things take place at the time of rating the post:
- first: cache is not yet full.
- second: post karma score after rating is higher than the lowest score of a post in cache.
```
But this would be actually rare because all cached posts get expired after [TIMEOUT](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/cache/PostRedisCache.java#L32).

# Environment variables
Backend requires four environment variables to be set:
- KARMA_APP_GATEWAY_REDIS_HOSTNAME
- KARMA_APP_GATEWAY_SECRET
- KARMA_APP_POSTS_HOST
- KARMA_APP_USERS_HOST

for details see: [application.yaml](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/resources/application.yaml)

Simply create .env and place it in the root of project.

For example:
```
KARMA_APP_GATEWAY_REDIS_HOSTNAME=karma-app-redis
KARMA_APP_GATEWAY_SECRET=BARDZO-POTĘŻNY-SEKRET-JAKI-DŁUGI
KARMA_APP_POSTS_HOST=karma-app-posts
KARMA_APP_USERS_HOST=karma-app-users
```
#### Important notes
KARMA_APP_GATEWAY_SECRET should have at least 32 bytes.

KARMA_APP_POSTS_HOST AND KARMA_APP_USERS_HOST should be the same as the ones in
[karma-app-posts](https://github.com/msik-404/karma-app-posts)
and
[karma-app-users](https://github.com/msik-404/karma-app-users/tree/main)
respectively.

# Building the project
To get target folder and build the project with maven simply run:
```
./mvnw clean package -DskipTests
```

If one would like to build the project with running the tests, one must have docker installed on their machine and run:
```
./mvnw clean package
```

# Tests
Docker is required to run tests locally because I use [Testcontainers for Java](https://java.testcontainers.org/).

Code that is directly communicating with redis is fully tested with integration tests.
Test reside in [src/test](https://github.com/msik-404/karma-app-gateway/tree/main/src/test)

# Starting the microservice | deployment for testing
To run entire application check out [karma-app-microservices](https://github.com/msik-404?tab=repositories)
which is repository with code for starting all microservices.

#### Starting only karma-app-gateway
To start just karma-app-gateway, one would also need to have already running [karma-app-posts](https://github.com/msik-404/karma-app-posts)
and [karma-app-users](https://github.com/msik-404/karma-app-users/tree/main), one can inspect readme's for information on
how to do this.

In this repository one can find [docker-compose-yaml](https://github.com/msik-404/karma-app-gateway/blob/main/docker-compose.yaml).

To start the microservice one should use provided bash scripts but pure docker can also be used.

### Bash scripts
Bash scripts can be found under [scripts](https://github.com/msik-404/karma-app-gateway/tree/main/scripts) folder.

Starting microservice: [start.sh](https://github.com/msik-404/karma-app-gateway/blob/main/scripts/start.sh)

Stopping microservice: [stop.sh](https://github.com/msik-404/karma-app-gateway/blob/main/scripts/stop.sh)

Cleaning after microservice: [clean.sh](https://github.com/msik-404/karma-app-gateway/blob/main/scripts/clean.sh)

To run the scripts make them executable for example:
```
sudo chmod 744 *.sh
```
and then use:
```
./start.sh
```
```
./stop.sh
```
```
./clean.sh
```

### Pure docker method
```
docker compose up
```