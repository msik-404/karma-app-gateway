package com.msik404.karmaappgateway.user.exception;

import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class DuplicateUsernameException extends RestFromGrpcException {

    public static final String Id = "DuplicateUsername";
    private static final String ERROR_MESSAGE = "Document with provided username exists.";

    public DuplicateUsernameException() {
        super(ERROR_MESSAGE);
    }

    @NonNull
    @Override
    public String getExceptionId() {
        return Id;
    }

    @NonNull
    @Override
    public StatusRuntimeException asStatusRuntimeException() {
        return Status.ALREADY_EXISTS
                .withDescription(getEncodedException())
                .asRuntimeException();
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, getMessage());
    }
}
