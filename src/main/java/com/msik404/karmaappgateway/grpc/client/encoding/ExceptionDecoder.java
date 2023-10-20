package com.msik404.karmaappgateway.grpc.client.encoding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import com.msik404.karmaappgateway.grpc.client.encoding.exception.BadEncodingException;
import com.msik404.karmaappgateway.grpc.client.exception.FailedValidationException;
import com.msik404.karmaappgateway.grpc.client.exception.UnsupportedRoleException;
import com.msik404.karmaappgateway.grpc.client.exception.UnsupportedVisibilityException;
import com.msik404.karmaappgateway.post.exception.FileProcessingException;
import com.msik404.karmaappgateway.post.exception.ImageNotFoundException;
import com.msik404.karmaappgateway.post.exception.PostNotFoundException;
import com.msik404.karmaappgateway.post.exception.RatingNotFoundException;
import com.msik404.karmaappgateway.user.exception.DuplicateEmailException;
import com.msik404.karmaappgateway.user.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappgateway.user.exception.DuplicateUsernameException;
import com.msik404.karmaappgateway.user.exception.UserNotFoundException;
import org.springframework.lang.NonNull;

public class ExceptionDecoder {

    @NonNull
    public static String decodeExceptionId(@NonNull String encodedException) throws BadEncodingException {

        // this pattern will match only the first occurrence;
        String regex = String.format("\\A.*?%s([^ ]+)", ExceptionEncoder.EXCEPTION_ID_PREFIX);
        var pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(encodedException);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new BadEncodingException(encodedException);
        }
    }

    @NonNull
    private static RestFromGrpcException decodeExceptionImpl(
            @NonNull String exceptionId,
            @NonNull String encodedException
    ) throws BadEncodingException {

        return switch (exceptionId) {

            case UnsupportedRoleException.Id -> new UnsupportedRoleException();
            case UnsupportedVisibilityException.Id -> new UnsupportedVisibilityException();

            case FileProcessingException.Id -> new FileProcessingException();

            case UserNotFoundException.Id -> new UserNotFoundException();
            case PostNotFoundException.Id -> new PostNotFoundException();
            case RatingNotFoundException.Id -> new RatingNotFoundException();
            case ImageNotFoundException.Id -> new ImageNotFoundException();

            case DuplicateEmailException.Id -> new DuplicateEmailException();
            case DuplicateUsernameException.Id -> new DuplicateUsernameException();
            case DuplicateUnexpectedFieldException.Id -> new DuplicateUnexpectedFieldException();

            case FailedValidationException.Id -> new FailedValidationException(encodedException);

            default -> throw new BadEncodingException(encodedException);
        };
    }

    @NonNull
    public static RestFromGrpcException decodeException(
            @NonNull String encodedException
    ) throws BadEncodingException {

        String exceptionId = decodeExceptionId(encodedException);

        return decodeExceptionImpl(exceptionId, encodedException);
    }

}
