package szte.flowboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing user.
 * Contains all fields that can be updated for a user.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDto {
    
    /** The first name of the user (required, 1-50 characters) */
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;
    
    /** The last name of the user (required, 1-50 characters) */
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;
    
    /** The email address of the user (required, valid email format, max 100 characters) */
    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be valid")
    @Size(max = 100, message = "Email address must not exceed 100 characters")
    private String emailAddress;
}



