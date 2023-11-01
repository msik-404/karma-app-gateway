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
All endpoints reside in [PostController](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/post/PostController.java)
and [UserController](https://github.com/msik-404/karma-app-gateway/blob/main/src/main/java/com/msik404/karmaappgateway/user/UserController.java)

These are all the supported endpoints.

#### GET guest/posts?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL=username=OPTIONAL
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
#### GET user/posts?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL&active=OPTIONAL_BOOL&hidden=OPTIONAL_BOOL
This endpoint is similar to guest one, but returns only posts created by logged-in user, therefore it requires user to
be logged-in. Arguments active and hidden can be set to true, if so both active and hidden posts are displayed. Of course
user can also set only active=true to see only active posts or only hidden=true to see only hidden posts. If bot active
and hidden are not set, by default only active posts are returned. This endpoint does not use cache.

#### GET user/posts/ratings?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_scored=OPTIONAL&username=OPTIONAL
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

#### GET mod/posts?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL=username=OPTIONAL&active=OPTIONAL_BOOL&hidden=OPTIONAL_BOOL
This endpoint works similar to guest/posts but mod user can also see hidden posts. User is required to be logged-in and
have mod or admin role.

#### GET mod/posts/ratings?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL=username=OPTIONAL&active=OPTIONAL_BOOL&hidden=OPTIONAL_BOOL
This endpoint works similar to user/posts/ratings but mod user can also see hidden posts. User is required to be 
logged-in and have mod or admin role.

#### GET admin/posts?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL=username=OPTIONAL&active=OPTIONAL_BOOL&hidden=OPTIONAL_BOOL&deleted=OPTIONAL_BOOL
This endpoint works similar to mod/posts but admin user can also see deleted posts. User is required to be logged-in and
have admin role.

#### GET admin/posts/ratings?size=OPTIONAL_DEFAULT_100&post_id=OPTIONAL&karma_score=OPTIONAL=username=OPTIONAL&active=OPTIONAL_BOOL&hidden=OPTIONAL_BOOL&deleted=OPTIONAL_BOOL
This endpoint works similar to mod/posts/ratings but admin user can also see deleted posts. User is required to be
logged-in and have admin role.

#### GET guests/posts/{postId}/image
This endpoint is used for getting image of a given post. If post is not found http error 404 with appropriate message is
returned. postId is SOME_24_CHAR_HEX_STRING. Image is returned as byte array with media type IMAGE_JPEG.

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

# Environment variables
Backend requires two environment variables to be set:
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