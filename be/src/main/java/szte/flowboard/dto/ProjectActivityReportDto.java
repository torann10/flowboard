package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectActivityReportDto {

    private String name;

    private LocalDate start;

    private LocalDate end;

    private LocalDateTime createdAt;

    private List<ProjectActivityReportLineItemDto> lines;
}
