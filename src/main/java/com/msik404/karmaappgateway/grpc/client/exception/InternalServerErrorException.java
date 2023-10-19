package com.msik404.karmaappgateway.grpc.client.exception;

public class InternalServerErrorException extends InternalRestException {

    public InternalServerErrorException(String message) {
        super(message);
    }

}
