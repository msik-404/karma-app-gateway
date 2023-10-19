package com.msik404.karmaappgateway.exception;

import com.msik404.karmaappgateway.grpc.client.exception.EncodableGrpcStatusException;

public abstract class RestFromGrpcException extends EncodableGrpcStatusException implements RestException {

    public RestFromGrpcException(String errorMessage) {
        super(errorMessage);
    }

}
