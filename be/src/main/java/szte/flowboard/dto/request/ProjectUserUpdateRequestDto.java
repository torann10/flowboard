package szte.flowboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.UserRole;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating an existing project-user relationship.
 * Allows updating the role and fee for a user's association with a project.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUserUpdateRequestDto {
    
    /** The role of the user in the project (required) */
    @NotNull(message = "User role is required")
    private UserRole role;

    /** The hourly fee for the user in this project (required) */
    @NotNull(message = "User fee is required")
    private Double fee;
}



