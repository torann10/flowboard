package szte.flowboard.service.report;

import org.springframework.stereotype.Service;
import szte.flowboard.dto.ProjectActivityReportDto;
import szte.flowboard.dto.ProjectActivityReportLineItemDto;
import szte.flowboard.dto.request.CreateProjectActivityReportRequestDto;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.repository.TaskRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class ProjectActivityReportGenerator {

    private final TaskRepository taskRepository;
    private final HTMLGenerator htmlGenerator;
    private final PDFGenerator pdfGenerator;

    public ProjectActivityReportGenerator(
            TaskRepository taskRepository,
            HTMLGenerator htmlGenerator,
            PDFGenerator pdfGenerator) {
        this.taskRepository = taskRepository;
        this.htmlGenerator = htmlGenerator;
        this.pdfGenerator = pdfGenerator;
    }

    public byte[] generate(CreateProjectActivityReportRequestDto report, ProjectEntity project) throws IOException {
        var finishedTasks = taskRepository
                .findByProjectIdAndFinishedAtBetween(
                        report.getProjectId(),
                        report.getStartDate().atStartOfDay(),
                        report.getEndDate().atStartOfDay().plusDays(1).minusSeconds(1));

        var projectActivitySummary = new ProjectActivityReportLineItemDto("Összesen", 0L, 0L, 0L);
        var projectActivityLineItems = new ArrayList<ProjectActivityReportLineItemDto>();

        for (var entry : finishedTasks) {
            var spentHours = entry.getTimeLogs()
                    .stream()
                    .mapToLong(t -> t.getLoggedTime().toMinutes())
                    .sum();
            var estimatedHours = entry.getStoryPointMapping()
                    .getTimeValue()
                    .toMinutes();
            var deviation = spentHours - estimatedHours;

            var line = new ProjectActivityReportLineItemDto(entry.getName(), spentHours, estimatedHours, deviation);

            projectActivitySummary.summarize(line);

            projectActivityLineItems.add(line);
        }

        projectActivityLineItems.add(projectActivitySummary);

        var activityReport = new ProjectActivityReportDto(
                project.getName(),
                report.getStartDate(),
                report.getEndDate(),
                LocalDateTime.now(),
                projectActivityLineItems);

        var html = htmlGenerator.generateFromProjectActivity(activityReport);
        return pdfGenerator.generatePdf(html);
    }
}

