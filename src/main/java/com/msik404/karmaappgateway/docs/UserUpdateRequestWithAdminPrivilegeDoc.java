package com.msik404.karmaappgateway.docs;

import com.msik404.karmaappgateway.user.Role;
import jakarta.validation.constraints.Email;
import org.springframework.lang.Nullable;

/**
 * This record is only used for swagger documentation. This is required because normally UserUpdateRequestWithAdminPrivilege
 * with nested structure is used. This nested structure is deserialized from flat structure but swagger cannot pick it up,
 * so this record exists.
 */
public record UserUpdateRequestWithAdminPrivilegeDoc(
        @Nullable String firstName,
        @Nullable String lastName,
        @Nullable String username,
        @Nullable @Email String email,
        @Nullable String password,
        @Nullable Role role) {
}
