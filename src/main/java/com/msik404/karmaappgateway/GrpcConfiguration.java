package com.msik404.karmaappgateway;

import com.msik404.karmaappposts.grpc.PostsGrpc;
import com.msik404.karmaappusers.grpc.UsersGrpc;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfiguration {

    @Value("${KarmaAppPosts.grpc.host}")
    private String postsHostname;

    @Value("${KarmaAppPosts.grpc.port}")
    private int postsPort;

    @Value("${KarmaAppUsers.grpc.host}")
    private String usersHostname;

    @Value("${KarmaAppUsers.grpc.port}")
    private int usersPort;

    @Bean(name = "postsChannel")
    Channel postsChannel() {
        return Grpc.newChannelBuilderForAddress(postsHostname, postsPort, InsecureChannelCredentials.create()).build();
    }

    @Bean(name = "usersChannel")
    Channel usersChannel() {
        return Grpc.newChannelBuilderForAddress(usersHostname, usersPort, InsecureChannelCredentials.create()).build();
    }

    @Bean
    PostsGrpc.PostsFutureStub postsStub(@Qualifier("postsChannel") Channel postsChannel) {
        return PostsGrpc.newFutureStub(postsChannel);
    }

    @Bean
    UsersGrpc.UsersFutureStub usersStub(@Qualifier("usersChannel") Channel usersChannel) {
        return UsersGrpc.newFutureStub(usersChannel);
    }

}
