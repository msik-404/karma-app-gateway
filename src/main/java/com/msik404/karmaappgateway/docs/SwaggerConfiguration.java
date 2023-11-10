package com.msik404.karmaappgateway.docs;

import java.util.ArrayList;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
@SecurityScheme(
        name = SwaggerConfiguration.AUTH,
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfiguration {

    public static final String AUTH = "Bearer Authentication";

    /**
     * This method is needed to allow sending multipart requests. For example, when an item is
     * created together with an image. If this is not set the request will return an exception with:
     * <p>
     * Resolved [org.springframework.web.HttpMediaTypeNotSupportedException: Content-Type
     * 'application/octet-stream' is not supported]
     *
     * @param converter
     */
    public SwaggerConfiguration(MappingJackson2HttpMessageConverter converter) {
        var supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        supportedMediaTypes.add(new MediaType("application", "octet-stream"));
        converter.setSupportedMediaTypes(supportedMediaTypes);
    }

}
