package com.msik404.karmaappgateway.grpc.client.exception;

public class UnsupportedVisibilityException extends RuntimeException {

    public UnsupportedVisibilityException() {
        super("Unsupported visibility provided.");
    }

}
