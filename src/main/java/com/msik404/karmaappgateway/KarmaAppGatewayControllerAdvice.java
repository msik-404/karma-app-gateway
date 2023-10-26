package com.msik404.karmaappgateway;

import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import com.msik404.karmaappgateway.exception.AbstractRestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class KarmaAppGatewayControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail authenticationException(AuthenticationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(RestFromGrpcException.class)
    public ProblemDetail restFromGrpcException(RestFromGrpcException ex) {
        return ex.getProblemDetail();
    }

    @ExceptionHandler(AbstractRestException.class)
    public ProblemDetail abstractRestException(AbstractRestException ex) {
        return ex.getProblemDetail();
    }

}
