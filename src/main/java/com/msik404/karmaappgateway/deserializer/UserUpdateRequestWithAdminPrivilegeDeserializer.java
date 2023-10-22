package com.msik404.karmaappgateway.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.msik404.karmaappgateway.user.Role;
import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaappgateway.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaappgateway.user.exception.BadRoleStringException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class UserUpdateRequestWithAdminPrivilegeDeserializer extends JsonDeserializer<UserUpdateRequestWithAdminPrivilege> {

    @Nullable
    private static String getNullableNodeValue(@NonNull JsonNode rootNode, @NonNull String fieldName) {

        JsonNode node = rootNode.get(fieldName);
        if (node == null) {
            return null;
        }
        return node.asText();
    }

    @Override
    public UserUpdateRequestWithAdminPrivilege deserialize(
            JsonParser p,
            DeserializationContext ctxt
    ) throws IOException, BadRoleStringException {

        JsonNode rootNode = p.getCodec().readTree(p);

        String firstName = getNullableNodeValue(rootNode, "firstName");
        String lastName = getNullableNodeValue(rootNode, "lastName");
        String username = getNullableNodeValue(rootNode, "username");
        String email = getNullableNodeValue(rootNode, "email");
        String password = getNullableNodeValue(rootNode, "password");
        String role = getNullableNodeValue(rootNode, "role");

        try {
            return new UserUpdateRequestWithAdminPrivilege(
                    new UserUpdateRequestWithUserPrivilege(firstName, lastName, username, email, password),
                    role == null ? null : Role.valueOf(role)
            );
        } catch (IllegalArgumentException ex) {
            throw new BadRoleStringException();
        }
    }

}
