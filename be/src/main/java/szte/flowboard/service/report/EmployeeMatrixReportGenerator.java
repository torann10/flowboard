package szte.flowboard.service.report;

import org.springframework.stereotype.Service;
import szte.flowboard.dto.request.CreateEmployeeMatrixReportRequestDto;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.repository.ProjectRepository;
import szte.flowboard.repository.TimeLogRepository;
import szte.flowboard.enums.UserRole;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating employee matrix reports.
 * Creates a matrix showing time logged by employees across projects managed by the user.
 * Only includes projects where the user has MAINTAINER role.
 */
@Service
public class EmployeeMatrixReportGenerator {

    private final TimeLogRepository timeLogRepository;
    private final ProjectRepository projectRepository;
    private final HTMLGenerator htmlGenerator;
    private final PDFGenerator pdfGenerator;

    public EmployeeMatrixReportGenerator(
            TimeLogRepository timeLogRepository,
            ProjectRepository projectRepository,
            HTMLGenerator htmlGenerator,
            PDFGenerator pdfGenerator) {
        this.timeLogRepository = timeLogRepository;
        this.projectRepository = projectRepository;
        this.htmlGenerator = htmlGenerator;
        this.pdfGenerator = pdfGenerator;
    }

    /**
     * Generates an employee matrix report PDF.
     * Aggregates time logs by user and project, then generates a matrix showing hours worked.
     *
     * @param report the employee matrix report request containing date range
     * @param userId the unique identifier of the user generating the report
     * @return the PDF as a byte array, or null if user has no projects with MAINTAINER role
     * @throws IOException if report generation fails
     */
    public byte[] generate(CreateEmployeeMatrixReportRequestDto report, UUID userId) throws IOException {
        var optionalProjects = projectRepository
                .findAllByProjectUsersUserIdAndProjectUsersRole(userId, UserRole.MAINTAINER);

        if (optionalProjects.isEmpty()) {
            return null;
        }

        var projectIds = optionalProjects.stream()
                .map(ProjectEntity::getId)
                .collect(Collectors.toSet());
        var timeLogs = timeLogRepository
                .findByTaskProjectIdInAndLogDateBetween(projectIds, report.getStartDate(), report.getEndDate());
        var projectTimeLogs = timeLogs.stream()
                .collect(Collectors.groupingBy(t -> t.getTask().getProject().getId()));
        var users = timeLogs.stream()
                .map(TimeLogEntity::getUser)
                .distinct().toList();
        var result = new ArrayList<ArrayList<String>>();
        var userHours = new HashMap<UUID, Double>();

        var nameColumn = new ArrayList<String>() {
            {
                add("Név");
            }
        };

        for (var projectUser : users) {
            nameColumn.add(projectUser.getFullName());
        }

        nameColumn.add("Összesen");
        result.add(nameColumn);

        for (var projectId : projectIds) {
            var entry = projectTimeLogs.get(projectId);
            var projectColumn = new ArrayList<String>();
            var project = optionalProjects.stream()
                    .filter(p -> p.getId().equals(projectId))
                    .findFirst();

            if (project.isEmpty()) {
                projectColumn.add("");
            } else {
                projectColumn.add(project.get().getName());
            }

            var projectHours = 0.0;

            for (var projectUser : users) {
                Double hours;

                if (entry == null || entry.isEmpty()) {
                    hours = 0.0;
                } else {
                    hours = entry.stream()
                            .filter(t -> t.getUser().getId() == projectUser.getId())
                            .mapToDouble(t -> t.getLoggedTime().toMinutes()).sum() / 60.0;
                }

                var userHour = userHours.getOrDefault(projectUser.getId(), 0.0);
                userHours.put(projectUser.getId(), userHour + hours);
                projectHours += hours;

                if (hours == 0) {
                    projectColumn.add("-");
                } else {
                    projectColumn.add(hours + " óra");
                }
            }

            if (projectHours == 0) {
                projectColumn.add("-");
            } else {
                projectColumn.add(projectHours + " óra");
            }

            result.add(projectColumn);
        }

        var sumColumn = new ArrayList<String>() {
            {
                add("Összesen");
            }
        };
        var sumTotal = 0.0;

        for (var projectUser : users) {
            var userHour = userHours.getOrDefault(projectUser.getId(), 0.0);

            if (userHour == 0) {
                sumColumn.add("-");
            } else {
                sumColumn.add(userHour + " óra");
            }

            sumTotal += userHour;
        }

        if (sumTotal == 0) {
            sumColumn.add("-");
        } else {
            sumColumn.add(sumTotal + " óra");
        }

        result.add(sumColumn);

        var html = htmlGenerator.generateFromMatrix(result);
        return pdfGenerator.generatePdf(html);
    }
}

