package com.msik404.karmaappgateway.user.exception;

import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class UserNotFoundException extends RestFromGrpcException {

    public static final String Id = "UserNotFound";
    private static final String ERROR_MESSAGE = "User was not found.";

    public UserNotFoundException() {
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
        return Status.NOT_FOUND
                .withDescription(getEncodedException())
                .asRuntimeException();
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, getMessage());
    }
}
