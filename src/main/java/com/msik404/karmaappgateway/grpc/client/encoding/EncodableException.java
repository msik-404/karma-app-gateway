package com.msik404.karmaappgateway.grpc.client.encoding;

import org.springframework.lang.NonNull;

public interface EncodableException {

    @NonNull
    String getEncodedException();

}
