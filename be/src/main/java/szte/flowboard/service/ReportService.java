package szte.flowboard.service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import szte.flowboard.dto.*;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.entity.ReportEntity;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.enums.ProjectType;
import szte.flowboard.enums.UserRole;
import szte.flowboard.repository.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;
    private final ProjectUserRepository projectUserRepository;
    private final TimeLogRepository timeLogRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final EntityManager entityManager;

    public ReportService(
            ReportRepository reportRepository,
            UserService userService,
            ProjectUserRepository projectUserRepository,
            TimeLogRepository timeLogRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository, EntityManager entityManager) {
        this.reportRepository = reportRepository;
        this.userService = userService;
        this.projectUserRepository = projectUserRepository;
        this.timeLogRepository = timeLogRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.entityManager = entityManager;
    }

    public UUID createEmployeeMatrix(CreateEmployeeMatrixReportRequestDto report, Authentication authentication)
            throws IOException {
        var user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        var optionalProjects = projectRepository
                .findAllByProjectUsersUserIdAndProjectUsersRole(user.get().getId(), UserRole.MAINTAINER);

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
                    .filter(p -> p.getId()
                            .equals(projectId))
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

        var html = generateHtmlFromReport(result);
        var data = generatePdfFromReport(html);

        return persistReport(null, user.get().getId(), report.getStartDate(), report.getEndDate(), "munkavallaloi_matrix", data);
    }

    public UUID createProjectActivityReport(CreateProjectActivityReportRequestDto report, Authentication authentication)
            throws IOException {
        var optionalUser = userService.getUserByAuthentication(authentication);

        if (optionalUser.isEmpty()) {
            return null;
        }

        var user = optionalUser.get();
        var project = userHasProjectAccess(report.getProjectId(), user.getId());

        if (project == null) {
            return null;
        }

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

        // Generate PDF
        var html = generateHtmlFromReport(activityReport);
        var data = generatePdfFromReport(html);

        return persistReport(
                project.getId(),
                user.getId(),
                report.getStartDate(),
                report.getEndDate(),
                "projekt_aktivitas",
                data);
    }

    public UUID createCOC(CreateCOCReportRequestDto report, Authentication authentication) throws IOException {
        var optionalUser = userService.getUserByAuthentication(authentication);

        if (optionalUser.isEmpty()) {
            return null;
        }

        var user = optionalUser.get();
        var project = userHasProjectAccess(report.getProjectId(), user.getId());

        if (project == null) {
            return null;
        }

        byte[] data;

        if (project.getType() == ProjectType.TIME_BASED) {
            data = createTimeBasedCocReport(report, project);
        } else {
            data = createStoryBasedCocReport(report, project);
        }


        return persistReport(project.getId(), user.getId(), report.getStartDate(), report.getEndDate(), "teljesitesi_igazolas", data);
    }

    public List<ReportEntity> findAllByUser(Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            return List.of();
        }

        return reportRepository.findByUserId(user.get().getId());
    }

    public URL getDownloadUrl(UUID reportId, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            return null;
        }

        var report = reportRepository.findByIdAndUserId(reportId, user.get().getId());

        if (report.isEmpty()) {
            return null;
        }

        try (var s3Presigner = getS3Presigner()) {
            var objectRequest = GetObjectRequest.builder()
                    .bucket("flowboard-report-bucket")
                    .key(reportId.toString())
                    .responseContentDisposition(report.get().contentDisposition())
                    .responseContentType("application/pdf")
                    .build();

            var presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5))
                    .getObjectRequest(objectRequest)
                    .build();

            var result = s3Presigner.presignGetObject(presignRequest);

            return result.url();
        } catch (S3Exception e) {
            return null;
        }
    }

    @Transactional
    public boolean deleteReport(UUID reportId, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        var result = user
                .filter(userEntity -> reportRepository.deleteByIdAndUserId(reportId, userEntity.getId()) == 1)
                .isPresent();

        if (!result) {
            return false;
        }

        try (var s3Client = getS3ClientBuilder().build()) {
            var deleteObject = DeleteObjectRequest.builder()
                    .bucket("flowboard-report-bucket")
                    .key(reportId.toString())
                    .build();

            s3Client.deleteObject(deleteObject);

            return true;
        } catch (S3Exception ignored) {
            return false;
        }
    }

    @Transactional
    public boolean renameReport(UUID reportId, String name, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        return user.filter(userEntity -> reportRepository
                .renameReportByIdAndUserId(name, reportId, userEntity.getId()) == 1).isPresent();
    }

    private byte[] createTimeBasedCocReport(CreateCOCReportRequestDto report, ProjectEntity project)
            throws IOException {
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

        // Generate PDF
        var html = generateHtmlFromReport(cocReport);
        return generatePdfFromReport(html);
    }

    private byte[] createStoryBasedCocReport(CreateCOCReportRequestDto report, ProjectEntity project) throws IOException {
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

        // Generate PDF
        var html = generateHtmlFromReport(cocReport);
        return generatePdfFromReport(html);
    }

    private ProjectEntity userHasProjectAccess(UUID projectId, UUID userId) {
        var optionalProject = projectRepository.findById(projectId);

        if (optionalProject.isEmpty()) {
            return null;
        }

        if (!projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER) &&
                !projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.MAINTAINER)) {
            return null;
        }

        return optionalProject.get();
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

    private String generateHtmlFromReport(ProjectActivityReportDto report) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/project-activity.mustache");
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(resource.getInputStream()), "coc-report");

        Map<String, Object> context = new HashMap<>();
        context.put("projectName", report.getName());
        context.put("start", report.getStart() != null ? report.getStart().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.")) : "");
        context.put("end", report.getEnd() != null ? report.getEnd().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.")) : "");
        context.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm")));
        context.put("taskCount", report.getLines().size() - 1);
        context.put("tasks", getLines(report));

        StringWriter writer = new StringWriter();
        mustache.execute(writer, context).flush();
        return writer.toString();
    }

    private String generateHtmlFromReport(COCReportDto report) throws IOException {
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
        context.put("createdAt", report.getCreatedAt() != null ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm")) : "");
        context.put("description", report.getDescription() != null ? report.getDescription() : "");
        context.put("lines", getLines(report));

        StringWriter writer = new StringWriter();
        mustache.execute(writer, context).flush();
        return writer.toString();
    }

    private static List<Map<String, Object>> getLines(ProjectActivityReportDto report) {
        List<Map<String, Object>> taskList = new ArrayList<>();

        for (var line : report.getLines()) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", line.getName());
            m.put("spent", line.getSpentMinutes() != null ? String.format("%.2f óra", line.getSpentMinutes() / 60.0) : "");
            m.put("estimated", line.getEstimatedMinutes() != null ? String.format("%.2f óra", line.getEstimatedMinutes() / 60.0) : "");
            m.put("deviation", line.getDeviation() != null ? String.format("%.2f óra", line.getDeviation() / 60.0) : "");
            taskList.add(m);
        }
        return taskList;
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

    private byte[] generatePdfFromReport(String html) {
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

    protected UUID persistReport(UUID projectId, UUID userId, LocalDate start, LocalDate end, String name, byte[] data) {
        var entity = new ReportEntity();

        if (projectId != null) {
            entity.setProject(entityManager.getReference(ProjectEntity.class, projectId));
        }

        entity.setUser(entityManager.getReference(UserEntity.class, userId));
        entity.setStart(start);
        entity.setEnd(end);
        entity.setName(name);

        reportRepository.save(entity);

        try (var s3Client = getS3ClientBuilder().build()) {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket("flowboard-report-bucket")
                    .key(entity.getId().toString())
                    .build();

            s3Client.putObject(objectRequest, RequestBody.fromBytes(data));
        } catch (S3Exception e) {
            return null;
        }

        return entity.getId();
    }

    private S3ClientBuilder getS3ClientBuilder() {
        return S3Client.builder().credentialsProvider(DefaultCredentialsProvider.builder().build()).region(Region.EU_CENTRAL_1);
    }

    private S3Presigner getS3Presigner() {
        return S3Presigner.builder().credentialsProvider(DefaultCredentialsProvider.builder().build()).region(Region.EU_CENTRAL_1).build();
    }
}
