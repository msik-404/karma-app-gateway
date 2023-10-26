package com.msik404.karmaappgateway.user.exception;

import com.msik404.karmaappgateway.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public class BadRoleStringException extends AbstractRestException {

    private static final String ERROR_MESSAGE = "Wrong role name provided. Use USER, MOD or ADMIN. Lowercase is accepted";

    public BadRoleStringException() {
        super(ERROR_MESSAGE);
    }

    @NonNull
    @Override
    public ProblemDetail getProblemDetail() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, getMessage());
    }

}
