package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for report summary information.
 * Used to transfer report metadata between the API layer and client applications.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportDto {
    
    /** The unique identifier of the report */
    private UUID id;
    
    /** The name of the report */
    private String name;
    
    /** The start date of the report period */
    private LocalDate start;
    
    /** The end date of the report period */
    private LocalDate end;
    
    /** The name of the project (null for employee matrix reports) */
    private String projectName;
    
    /** The timestamp when the report was created */
    private LocalDateTime createdAt;
}

