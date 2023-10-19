package com.msik404.karmaappgateway.grpc.client.encoding.exception;

import com.msik404.karmaappgateway.grpc.client.exception.InternalRestException;

public class BadEncodingException extends InternalRestException {

    private static final String ERROR_MESSAGE = "StatusException has wrong format.";

    public BadEncodingException() {
        super(ERROR_MESSAGE);
    }

}
