package szte.flowboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.UserRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for creating a new project-user relationship.
 * Associates a user with a project and defines their role and fee.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUserCreateRequestDto {
    
    /** The unique identifier of the project */
    private UUID projectId;
    
    /** The unique identifier of the user */
    private UUID userId;
    
    /** The role of the user in the project (required) */
    @NotNull(message = "User role is required")
    private UserRole role;

    /** The hourly fee for the user in this project (required) */
    @NotNull(message = "User fee is required")
    private Double fee;
}



