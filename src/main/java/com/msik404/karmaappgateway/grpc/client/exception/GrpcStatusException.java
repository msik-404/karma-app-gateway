package com.msik404.karmaappgateway.grpc.client.exception;

import io.grpc.StatusRuntimeException;
import org.springframework.lang.NonNull;

public interface GrpcStatusException {

    @NonNull
    String getExceptionId();

    @NonNull
    StatusRuntimeException asStatusRuntimeException();

}
