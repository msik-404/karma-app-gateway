package com.msik404.karmaappgateway.auth.exception;

import com.msik404.karmaappgateway.grpc.client.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class InsufficientRoleException extends AbstractRestException {

    public InsufficientRoleException(@NonNull String errorMessage) {
        super(errorMessage);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                getMessage()
        );
    }

}
