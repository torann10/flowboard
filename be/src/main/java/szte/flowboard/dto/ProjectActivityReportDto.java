package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for project activity report data.
 * Contains task activity information including time spent, estimated time, and deviations.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectActivityReportDto {

    /** The name of the project */
    private String name;

    /** The start date of the report period */
    private LocalDate start;

    /** The end date of the report period */
    private LocalDate end;

    /** The timestamp when the report was created */
    private LocalDateTime createdAt;

    /** The list of task activity line items */
    private List<ProjectActivityReportLineItemDto> lines;
}
