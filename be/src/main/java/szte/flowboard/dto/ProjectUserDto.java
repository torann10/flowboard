package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for project-user relationship information.
 * Represents the association between a user and a project, including role and fee.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUserDto {
    
    /** The unique identifier of the project-user relationship */
    private UUID id;
    
    /** The unique identifier of the project */
    private UUID projectId;
    
    /** The unique identifier of the user */
    private UUID userId;
    
    /** The role of the user in the project (MAINTAINER, REPORTER, or DEVELOPER) */
    private UserRole role;
    
    /** The hourly fee for the user in this project */
    private Double fee;
    
    /** The username of the user who created the relationship */
    private String createdBy;
    
    /** The timestamp when the relationship was created */
    private LocalDateTime createdAt;
    
    /** The username of the user who last modified the relationship */
    private String lastModifiedBy;
    
    /** The timestamp when the relationship was last modified */
    private LocalDateTime lastModifiedAt;
}

