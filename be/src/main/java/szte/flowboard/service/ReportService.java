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
import szte.flowboard.dto.ReportCreateRequestDto;
import szte.flowboard.dto.ReportDto;
import szte.flowboard.entity.ReportEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.enums.UserRole;
import szte.flowboard.repository.*;

import java.io.*;
import java.time.Instant;
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

    public byte[] create(ReportCreateRequestDto report, Authentication authentication) {
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

        for(var entry : userTimeLogs.entrySet()) {
            var projectUser = projectUserRepository.findByUserIdAndProjectId(entry.getKey(), report.getProjectId());

            if(projectUser.isEmpty()) {
                continue;
            }

            var timeLog = entry.getValue();
            var hours = timeLog.stream().mapToDouble(t ->t.getLoggedTime().toMinutes()).sum()/60.0;
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
        return generatePdfFromReport(cocReport);
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

    public byte[] generatePdfFromReport(COCReportDto report) {
        try {
            String html = generateHtmlFromReport(report);
            
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
