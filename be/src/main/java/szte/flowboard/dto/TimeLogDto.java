package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeLogDto {
    
    private UUID id;
    private UUID taskId;
    private UUID userId;
    private Duration loggedTime;
    private boolean isBillable;
    private LocalDate logDate;
    private String createdBy;
    private LocalDate createAt;
    private String lastModifiedBy;
    private LocalDate lastModifiedAt;
}

