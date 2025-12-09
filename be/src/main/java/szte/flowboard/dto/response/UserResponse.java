package szte.flowboard.dto.response;

import java.util.UUID;

/**
 * Response DTO for user information.
 * Used to return user data to client applications.
 *
 * @param id the unique identifier of the user
 * @param keycloakId the Keycloak user ID
 * @param firstName the user's first name
 * @param lastName the user's last name
 * @param emailAddress the user's email address
 */
public record UserResponse(
        UUID id,
        String keycloakId,
        String firstName,
        String lastName,
        String emailAddress
) {
}

