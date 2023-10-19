package com.msik404.karmaappgateway.converter;

import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ObjectIdConverter implements Converter<String, ObjectId> {

    @Override
    public ObjectId convert(@NonNull final String idHexString) {
        return new ObjectId(idHexString);
    }

}
