package szte.flowboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating a project activity report.
 * Contains project ID and date range for which to generate the report.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProjectActivityReportRequestDto {

    /** The unique identifier of the project for the report */
    private UUID projectId;

    /** The start date of the report period */
    private LocalDate startDate;

    /** The end date of the report period */
    private LocalDate endDate;

}



