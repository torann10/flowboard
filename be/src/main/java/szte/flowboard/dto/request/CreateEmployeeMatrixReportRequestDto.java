package szte.flowboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for creating an employee matrix report.
 * Contains the date range for which to generate the report.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateEmployeeMatrixReportRequestDto {

    /** The start date of the report period */
    private LocalDate startDate;

    /** The end date of the report period */
    private LocalDate endDate;
    
}



