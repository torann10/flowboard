package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    
    private String id;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String keycloakId;
    private String createdBy;
    private LocalDate createdAt;
    private String lastModifiedBy;
    private LocalDate lastModifiedAt;
}

