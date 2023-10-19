package com.msik404.karmaappgateway.grpc.client.encoding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.msik404.karmaappgateway.exception.RestFromGrpcException;
import com.msik404.karmaappgateway.grpc.client.encoding.exception.BadEncodingException;
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
    public static String decodeExceptionId(@NonNull final String encodedException) throws BadEncodingException {

        // this pattern will match only the first occurrence;
        final String regex = String.format("\\A.*?%s([^ ]+)", ExceptionEncoder.EXCEPTION_ID_PREFIX);
        final var pattern = Pattern.compile(regex);

        final Matcher matcher = pattern.matcher(encodedException);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new BadEncodingException();
        }
    }

    @NonNull
    private static RestFromGrpcException decodeExceptionImpl(
            @NonNull final String exceptionId
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

            default -> throw new BadEncodingException();
        };
    }

    @NonNull
    public static RestFromGrpcException decodeException(
            @NonNull final String encodedException
    ) throws BadEncodingException {

        final String exceptionId = decodeExceptionId(encodedException);

        return decodeExceptionImpl(exceptionId);
    }

}
