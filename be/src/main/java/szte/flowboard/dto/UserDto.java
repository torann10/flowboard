package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for user information.
 * Used to transfer user data between the API layer and client applications.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    
    /** The unique identifier of the user */
    private UUID id;
    
    /** The first name of the user */
    private String firstName;
    
    /** The last name of the user */
    private String lastName;
    
    /** The email address of the user */
    private String emailAddress;
    
    /** The Keycloak user ID associated with this user */
    private String keycloakId;
    
    /** The username of the user who created this user record */
    private String createdBy;
    
    /** The timestamp when the user record was created */
    private LocalDateTime createdAt;
    
    /** The username of the user who last modified this user record */
    private String lastModifiedBy;
    
    /** The timestamp when the user record was last modified */
    private LocalDateTime lastModifiedAt;
}

