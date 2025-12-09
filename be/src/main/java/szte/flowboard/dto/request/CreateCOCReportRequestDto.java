package szte.flowboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating a Certificate of Completion (COC) report.
 * Contains project ID, date range, and description for the report.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCOCReportRequestDto {

    /** The unique identifier of the project for the report */
    private UUID projectId;

    /** The start date of the report period */
    private LocalDate startDate;

    /** The end date of the report period */
    private LocalDate endDate;
    
    /** The description of the work performed (required, max 1000 characters) */
    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}



