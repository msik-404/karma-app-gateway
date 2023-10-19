package com.msik404.karmaappgateway.user.exception;

import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class DuplicateUnexpectedFieldException extends RestFromGrpcException {

    public static final String Id = "DuplicateUnexpectedField";
    private static final String ERROR_MESSAGE = "Document with provided field value exists. Specific field is unknown.";

    public DuplicateUnexpectedFieldException() {
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
