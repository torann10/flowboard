package szte.flowboard.dto.response;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String keycloakId,
        String firstName,
        String lastName,
        String emailAddress
) {
}

