package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for company information.
 * Represents customer or contractor company details used in projects and reports.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDto {

    /** The name of the company */
    private String name;

    /** The address of the company */
    private String address;
}
