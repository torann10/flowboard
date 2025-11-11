package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.entity.CompanyEntity;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class COCReportDto {

    private LocalDate start;

    private LocalDate end;

    private LocalDate createdAt;

    private CompanyEntity customer;

    private CompanyEntity contractor;

    private List<COCReportLineItemDto> lines;

    private String description;
}
