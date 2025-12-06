package szte.flowboard.service.report;

import org.springframework.stereotype.Service;
import szte.flowboard.dto.COCReportDto;
import szte.flowboard.dto.COCReportLineItemDto;
import szte.flowboard.dto.request.CreateCOCReportRequestDto;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.enums.ProjectType;
import szte.flowboard.repository.ProjectUserRepository;
import szte.flowboard.repository.TaskRepository;
import szte.flowboard.repository.TimeLogRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class COCReportGenerator {

    private final TimeLogRepository timeLogRepository;
    private final TaskRepository taskRepository;
    private final ProjectUserRepository projectUserRepository;
    private final HTMLGenerator htmlGenerator;
    private final PDFGenerator pdfGenerator;

    public COCReportGenerator(
            TimeLogRepository timeLogRepository,
            TaskRepository taskRepository,
            ProjectUserRepository projectUserRepository,
            HTMLGenerator htmlGenerator,
            PDFGenerator pdfGenerator) {
        this.timeLogRepository = timeLogRepository;
        this.taskRepository = taskRepository;
        this.projectUserRepository = projectUserRepository;
        this.htmlGenerator = htmlGenerator;
        this.pdfGenerator = pdfGenerator;
    }

    public byte[] generate(CreateCOCReportRequestDto report, ProjectEntity project) throws IOException {
        if (project.getType() == ProjectType.TIME_BASED) {
            return generateTimeBased(report, project);
        } else {
            return generateStoryBased(report, project);
        }
    }

    private byte[] generateTimeBased(CreateCOCReportRequestDto report, ProjectEntity project) throws IOException {
        var timeLogs = timeLogRepository
                .findAllByTaskProjectIdAndLogDateBetween(
                        report.getProjectId(),
                        report.getStartDate(),
                        report.getEndDate());
        var userTimeLogs = timeLogs.stream()
                .collect(Collectors.groupingBy(t -> t.getUser().getId()));

        var cocSummary = new COCReportLineItemDto("Összesen", null, null, 0.0, 0.0, 0.0, null);
        var cocLineItems = new ArrayList<COCReportLineItemDto>();

        for (var entry : userTimeLogs.entrySet()) {
            var projectUser = projectUserRepository
                    .findByUserIdAndProjectId(entry.getKey(), report.getProjectId());

            if (projectUser.isEmpty()) {
                continue;
            }

            var timeLog = entry.getValue();
            var hours = timeLog.stream().mapToDouble(t -> t.getLoggedTime().toMinutes()).sum() / 60.0;
            var unitPrice = projectUser.get().getFee();
            var netPrice = unitPrice * hours;
            var grossPrice = netPrice * 1.27;
            var vatPrice = grossPrice - netPrice;

            var result = new COCReportLineItemDto(
                    projectUser.get().getUser().getFullName(),
                    hours, "óra",
                    netPrice, vatPrice,
                    grossPrice,
                    unitPrice);

            cocSummary.summarize(result);
            cocLineItems.add(result);
        }

        cocLineItems.add(cocSummary);

        var cocReport = new COCReportDto(
                report.getStartDate(),
                report.getEndDate(),
                LocalDateTime.now(),
                project.getCustomer(),
                project.getContractor(),
                cocLineItems,
                report.getDescription());

        var html = htmlGenerator.generateFromCOC(cocReport);
        return pdfGenerator.generatePdf(html);
    }

    private byte[] generateStoryBased(CreateCOCReportRequestDto report, ProjectEntity project) throws IOException {
        var tasks = taskRepository
                .findByProjectIdAndFinishedAtBetween(
                        report.getProjectId(),
                        report.getStartDate().atStartOfDay(),
                        report.getEndDate().atStartOfDay().plusDays(1).minusSeconds(1));

        var cocSummary = new COCReportLineItemDto("Összesen", null, null, 0.0, 0.0, 0.0, null);
        var cocLineItems = new ArrayList<COCReportLineItemDto>();
        var unitPrice = project.getStoryPointFee();

        for (var entry : tasks) {
            var storyPoints = entry.getStoryPointMapping().getStoryPoints();
            var netPrice = unitPrice * storyPoints;
            var grossPrice = netPrice * 1.27;
            var vatPrice = grossPrice - netPrice;

            var result = new COCReportLineItemDto(
                    entry.getName(),
                    storyPoints.doubleValue(),
                    "story pont",
                    netPrice,
                    vatPrice,
                    grossPrice,
                    unitPrice);

            cocSummary.summarize(result);
            cocLineItems.add(result);
        }

        cocLineItems.add(cocSummary);

        var cocReport = new COCReportDto(
                report.getStartDate(),
                report.getEndDate(),
                LocalDateTime.now(),
                project.getCustomer(),
                project.getContractor(),
                cocLineItems,
                report.getDescription());

        var html = htmlGenerator.generateFromCOC(cocReport);
        return pdfGenerator.generatePdf(html);
    }
}

