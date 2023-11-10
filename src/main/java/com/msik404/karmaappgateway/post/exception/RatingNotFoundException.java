package com.msik404.karmaappgateway.post.exception;

import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class RatingNotFoundException extends RestFromGrpcException {

    public static final String Id = "RatingNotFound";
    public static final String ERROR_MESSAGE = "Rating with provided post_id and user_id was not found.";

    public RatingNotFoundException() {
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
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, getExceptionId());
    }
}
