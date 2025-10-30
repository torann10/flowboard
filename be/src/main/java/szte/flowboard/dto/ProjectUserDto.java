package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUserDto {
    
    private String id;
    private String projectId;
    private String userId;
    private UserRole role;
    private String createdBy;
    private LocalDate createdAt;
    private String lastModifiedBy;
    private LocalDate lastModifiedAt;
}

