package com.msik404.karmaappgateway.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

public interface RestException {

    @NonNull
    ProblemDetail getProblemDetail();

}
