package com.msik404.karmaappgateway.grpc.client.encoding.exception;

import com.msik404.karmaappgateway.grpc.client.exception.InternalRestException;
import org.springframework.lang.NonNull;

public class BadEncodingException extends InternalRestException {

    private static final String ERROR_MESSAGE = "StatusException has wrong format.";

    public BadEncodingException(@NonNull String errorMessage) {
        super(String.format("%s : %s", ERROR_MESSAGE, errorMessage));
    }

}
