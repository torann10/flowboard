package szte.flowboard.service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import szte.flowboard.dto.COCReportDto;
import szte.flowboard.dto.COCReportLineItemDto;
import szte.flowboard.dto.CreateCOCReportRequestDto;
import szte.flowboard.dto.CreateEmployeeMatrixReportRequestDto;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.entity.ReportEntity;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.enums.UserRole;
import szte.flowboard.repository.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ProjectUserRepository projectUserRepository;
    private final TimeLogRepository timeLogRepository;
    private final ProjectRepository projectRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository, UserService userService, ProjectUserRepository projectUserRepository, TimeLogRepository timeLogRepository, ProjectRepository projectRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.projectUserRepository = projectUserRepository;
        this.timeLogRepository = timeLogRepository;
        this.projectRepository = projectRepository;
    }

    public byte[] createEmployeeMatrix(CreateEmployeeMatrixReportRequestDto report, Authentication authentication) throws IOException {
        var user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        var optionalProjects = projectRepository.findAllByProjectUsersUserIdAndProjectUsersRole(user.get().getId(), UserRole.MAINTAINER);

        if (optionalProjects.isEmpty()) {
            return null;
        }

        var projectIds = optionalProjects.stream().map(ProjectEntity::getId).collect(Collectors.toSet());
        var timeLogs = timeLogRepository.findByTaskProjectIdInAndLogDateBetween(projectIds, report.getStartDate(), report.getEndDate());
        var projectTimeLogs = timeLogs.stream().collect(Collectors.groupingBy(t -> t.getTask().getProject().getId()));
        var users = timeLogs.stream().map(TimeLogEntity::getUser).distinct().toList();
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
            var project = optionalProjects.stream().filter(p -> p.getId().equals(projectId)).findFirst();

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
                    hours = entry.stream().filter(t -> t.getUser().getId() == projectUser.getId()).mapToDouble(t -> t.getLoggedTime().toMinutes()).sum() / 60.0;;
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

        var html = generateHtmlFromReport(result);

        return generatePdfFromReport(html);
    }

    public byte[] createCOC(CreateCOCReportRequestDto report, Authentication authentication) throws IOException {
        var user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        var optionalProject = projectRepository.findById(report.getProjectId());

        if (optionalProject.isEmpty()) {
            return null;
        }

        if (!projectUserRepository.existsByUserIdAndProjectIdAndRole(user.get().getId(), report.getProjectId(), UserRole.REPORTER) &&
                !projectUserRepository.existsByUserIdAndProjectIdAndRole(user.get().getId(), report.getProjectId(), UserRole.MAINTAINER)) {
            return null;
        }

        var timeLogs = timeLogRepository.findAllByTaskProjectIdAndLogDateBetween(report.getProjectId(), report.getStartDate(), report.getEndDate());
        var userTimeLogs = timeLogs.stream().collect(Collectors.groupingBy(t -> t.getUser().getId()));

        var cocSummary = new COCReportLineItemDto();
        cocSummary.setName("Összesen");
        cocSummary.setNetPrice(0.0);
        cocSummary.setVatPrice(0.0);
        cocSummary.setGrossPrice(0.0);
        var cocLineItems = new ArrayList<COCReportLineItemDto>();

        for (var entry : userTimeLogs.entrySet()) {
            var projectUser = projectUserRepository.findByUserIdAndProjectId(entry.getKey(), report.getProjectId());

            if (projectUser.isEmpty()) {
                continue;
            }

            var timeLog = entry.getValue();
            var hours = timeLog.stream().mapToDouble(t -> t.getLoggedTime().toMinutes()).sum() / 60.0;
            var unitPrice = projectUser.get().getFee();
            var netPrice = unitPrice * hours;
            var grossPrice = netPrice * 1.27;
            var vatPrice = grossPrice - netPrice;

            var result = new COCReportLineItemDto(projectUser.get().getUser().getFullName(), hours, "óra", netPrice, vatPrice, grossPrice, unitPrice);

            cocSummary.summarize(result);
            cocLineItems.add(result);
        }

        cocLineItems.add(cocSummary);
        var project = optionalProject.get();

        var cocReport = new COCReportDto(report.getStartDate(), report.getEndDate(), LocalDate.now(), project.getCustomer(), project.getContractor(), cocLineItems, report.getDescription());

        // Generate PDF
        var html = generateHtmlFromReport(cocReport);

        return generatePdfFromReport(html);
    }

    private String generateHtmlFromReport(ArrayList<ArrayList<String>> matrix) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/employee-matrix.mustache");
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(resource.getInputStream()), "coc-report");
        Map<String, Object> context = new HashMap<>();

        int cols = matrix.size();
        int rows = matrix.getFirst().size();

        ArrayList<ArrayList<String>> transposed = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            ArrayList<String> newRow = new ArrayList<>();
            for (int c = 0; c < cols; c++) {
                newRow.add(matrix.get(c).get(r));
            }
            transposed.add(newRow);
        }

        List<Map<String, Object>> rowList = new ArrayList<>();

        for (var row : transposed) {
            Map<String, Object> rowMap = new HashMap<>();
            rowMap.put("cells", row);
            rowList.add(rowMap);
        }

        context.put("matrix", rowList);

        StringWriter writer = new StringWriter();
        mustache.execute(writer, context).flush();
        return writer.toString();
    }

    public String generateHtmlFromReport(COCReportDto report) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/coc-report.mustache");
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(resource.getInputStream()), "coc-report");

        Map<String, Object> context = new HashMap<>();
        context.put("customer", Map.of(
                "name", report.getCustomer() != null ? report.getCustomer().getName() : "",
                "address", report.getCustomer() != null ? report.getCustomer().getAddress() : ""
        ));
        context.put("contractor", Map.of(
                "name", report.getContractor() != null ? report.getContractor().getName() : "",
                "address", report.getContractor() != null ? report.getContractor().getAddress() : ""
        ));
        context.put("start", report.getStart() != null ? report.getStart().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.")) : "");
        context.put("end", report.getEnd() != null ? report.getEnd().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.")) : "");
        context.put("createdAt", report.getCreatedAt() != null ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.")) : "");
        context.put("description", report.getDescription() != null ? report.getDescription() : "");
        context.put("lines", getLines(report));

        StringWriter writer = new StringWriter();
        mustache.execute(writer, context).flush();
        return writer.toString();
    }

    private static List<Map<String, Object>> getLines(COCReportDto report) {
        List<Map<String, Object>> lines = new ArrayList<>();
        if (report.getLines() != null) {
            for (COCReportLineItemDto line : report.getLines()) {
                Map<String, Object> lineMap = new HashMap<>();
                lineMap.put("name", line.getName() != null ? line.getName() : "");
                lineMap.put("quantity", line.getQuantity() != null ? String.format("%.2f", line.getQuantity()) : "");
                lineMap.put("unit", line.getUnit());
                lineMap.put("unitPrice", line.getUnitPrice() != null ? String.format("%,.0f Ft", line.getUnitPrice()) : "");
                lineMap.put("netPrice", line.getNetPrice() != null ? String.format("%,.0f Ft", line.getNetPrice()) : "");
                lineMap.put("vatPrice", line.getVatPrice() != null ? String.format("%,.0f Ft", line.getVatPrice()) : "");
                lineMap.put("grossPrice", line.getGrossPrice() != null ? String.format("%,.0f Ft", line.getGrossPrice()) : "");
                lines.add(lineMap);
            }
        }
        return lines;
    }

    public byte[] generatePdfFromReport(String html) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.useFont(ResourceUtils.getFile("classpath:fonts/PTMono-Regular.ttf"), "PT Mono");
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF from report", e);
        }
    }

    public List<ReportEntity> findAllByUser(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);

        if (user.isEmpty()) {
            return List.of();
        }

        return reportRepository.findByUserId(user.get().getId());
    }

    public Optional<ReportEntity> findByIdAndUser(UUID id, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);

        if (user.isEmpty()) {
            return Optional.empty();
        }

        return reportRepository.findByIdAndUserId(id, user.get().getId());
    }

    public ReportEntity update(ReportEntity report, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        report.setUserId(user.get().getId());

        return reportRepository.save(report);
    }

    public void delete(UUID id) {
        reportRepository.deleteById(id);
    }

    public boolean existsByIdAndUser(UUID id, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);

        if (user.isEmpty()) {
            return false;
        }

        return reportRepository.existsByIdAndUserId(id, user.get().getId());
    }

    public long countByUser(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);

        if (user.isEmpty()) {
            return 0;
        }

        return reportRepository.countByUserId(user.get().getId());
    }

    private String getKeycloakIdFromAuthentication(Authentication authentication) {
        return (String) ((Jwt) authentication.getPrincipal()).getClaims().get("sub");
    }
}
