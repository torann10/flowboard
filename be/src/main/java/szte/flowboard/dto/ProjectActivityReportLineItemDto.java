package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectActivityReportLineItemDto {

    private String name;

    private Long spentMinutes;

    private Long estimatedMinutes;

    private Long deviation;

    public void summarize(ProjectActivityReportLineItemDto other) {
        this.spentMinutes += other.spentMinutes;
        this.estimatedMinutes += other.estimatedMinutes;
        this.deviation += other.deviation;
    }

}
