package com.msik404.karmaappgateway.grpc.client.exception;

import com.msik404.karmaappgateway.exception.RestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public abstract class InternalRestException extends RuntimeException implements RestException {

    public InternalRestException(String errorMessage) {
        super(errorMessage);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, getMessage());
    }

}
