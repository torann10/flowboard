package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportDto {
    
    private UUID id;
    private String name;
    private LocalDate start;
    private LocalDate end;
    private String projectName;
    private LocalDateTime createdAt;
}

