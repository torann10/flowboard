package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.entity.CompanyEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Certificate of Completion (COC) report data.
 * Contains billing information including customer, contractor, line items, and pricing details.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class COCReportDto {

    /** The start date of the report period */
    private LocalDate start;

    /** The end date of the report period */
    private LocalDate end;

    /** The timestamp when the report was created */
    private LocalDateTime createdAt;

    /** The customer company information */
    private CompanyEntity customer;

    /** The contractor company information */
    private CompanyEntity contractor;

    /** The list of billing line items */
    private List<COCReportLineItemDto> lines;

    /** The description of the work performed */
    private String description;
}
