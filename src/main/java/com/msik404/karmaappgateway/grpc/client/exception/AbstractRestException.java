package com.msik404.karmaappgateway.grpc.client.exception;

import com.msik404.karmaappgateway.exception.RestException;

public abstract class AbstractRestException extends RuntimeException implements RestException {
    public AbstractRestException(String errorMessage) {
        super(errorMessage);
    }

}
