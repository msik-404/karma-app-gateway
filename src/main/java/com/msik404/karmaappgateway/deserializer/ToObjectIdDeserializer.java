package com.msik404.karmaappgateway.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bson.types.ObjectId;
import org.springframework.lang.NonNull;

public class ToObjectIdDeserializer extends JsonDeserializer<ObjectId> {

    @Override
    public ObjectId deserialize(
            @NonNull JsonParser p,
            @NonNull DeserializationContext ctxt) throws IOException {

        String objectIdHexString = p.readValueAs(String.class);

        if (objectIdHexString == null) {
            return null;
        }

        return new ObjectId(objectIdHexString);
    }

}
