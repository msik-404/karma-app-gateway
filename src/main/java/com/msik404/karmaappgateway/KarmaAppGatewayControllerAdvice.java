package com.msik404.karmaappgateway;

import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import com.msik404.karmaappgateway.grpc.client.exception.InternalRestException;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class KarmaAppGatewayControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RestFromGrpcException.class)
    public ProblemDetail restFromGrpcException(@NonNull RestFromGrpcException ex) {
        return ex.getProblemDetail();
    }

    @ExceptionHandler(InternalRestException.class)
    public ProblemDetail internalRestException(@NonNull InternalRestException ex) {
        return ex.getProblemDetail();
    }

}
