package com.msik404.karmaappgateway.post.exception;

import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class FileProcessingException extends RestFromGrpcException {

    public final static String Id = "FileProcessing";
    public final static String ERROR_MESSAGE = "File could not be processed for some reason";

    public FileProcessingException() {
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
        return Status.INTERNAL
                .withDescription(getEncodedException())
                .asRuntimeException();
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, getMessage());
    }
}
