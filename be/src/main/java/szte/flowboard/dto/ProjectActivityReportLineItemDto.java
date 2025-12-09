package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for a single line item in a project activity report.
 * Represents task activity information including time spent, estimated time, and deviation.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectActivityReportLineItemDto {

    /** The name of the task */
    private String name;

    /** The time spent on the task in minutes */
    private Long spentMinutes;

    /** The estimated time for the task in minutes */
    private Long estimatedMinutes;

    /** The deviation (spent - estimated) in minutes */
    private Long deviation;

    /**
     * Adds the time values from another line item to this one (for summary calculations).
     *
     * @param other the other line item to summarize
     */
    public void summarize(ProjectActivityReportLineItemDto other) {
        this.spentMinutes += other.spentMinutes;
        this.estimatedMinutes += other.estimatedMinutes;
        this.deviation += other.deviation;
    }

}
