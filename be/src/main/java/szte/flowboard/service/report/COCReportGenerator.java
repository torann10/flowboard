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

/**
 * Service for generating Certificate of Completion (COC) reports.
 * Creates billing reports for projects, supporting both time-based and story-point-based projects.
 * Calculates net, VAT, and gross prices based on project type and user fees.
 */
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

    /**
     * Generates a COC report PDF based on the project type.
     * Delegates to time-based or story-point-based generation methods.
     *
     * @param report the COC report request containing project and date range
     * @param project the project entity for which to generate the report
     * @return the PDF as a byte array
     * @throws IOException if report generation fails
     */
    public byte[] generate(CreateCOCReportRequestDto report, ProjectEntity project) throws IOException {
        if (project.getType() == ProjectType.TIME_BASED) {
            return generateTimeBased(report, project);
        } else {
            return generateStoryBased(report, project);
        }
    }

    /**
     * Generates a COC report for a time-based project.
     * Calculates billing based on hours logged by users and their fees.
     *
     * @param report the COC report request containing project and date range
     * @param project the time-based project entity
     * @return the PDF as a byte array
     * @throws IOException if report generation fails
     */
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

    /**
     * Generates a COC report for a story-point-based project.
     * Calculates billing based on completed tasks' story points and the project's story point fee.
     *
     * @param report the COC report request containing project and date range
     * @param project the story-point-based project entity
     * @return the PDF as a byte array
     * @throws IOException if report generation fails
     */
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

