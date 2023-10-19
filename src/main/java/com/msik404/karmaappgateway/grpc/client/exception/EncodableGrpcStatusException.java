package com.msik404.karmaappgateway.grpc.client.exception;

import com.msik404.karmaappgateway.grpc.client.encoding.EncodableException;
import com.msik404.karmaappgateway.grpc.client.encoding.ExceptionEncoder;
import org.springframework.lang.NonNull;

public abstract class EncodableGrpcStatusException extends RuntimeException implements EncodableException, GrpcStatusException {

    public EncodableGrpcStatusException(@NonNull String errorMessage) {
        super(errorMessage);
    }

    @NonNull
    @Override
    public String getEncodedException() {
        return ExceptionEncoder.encode(getExceptionId(), getMessage());
    }

}
