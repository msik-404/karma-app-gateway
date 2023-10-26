package com.msik404.karmaappgateway.exception;

public abstract class AbstractRestException extends RuntimeException implements RestException {
    public AbstractRestException(String errorMessage) {
        super(errorMessage);
    }

}
