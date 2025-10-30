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
public class ReportDto {
    
    private String id;
    private String projectId;
    private String userId;
    private String taskId;
    private String description;
    private LocalDate reportDate;
    private String createdBy;
    private LocalDate createdAt;
    private String lastModifiedBy;
    private LocalDate lastModifiedAt;
}

