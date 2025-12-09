package szte.flowboard.service;

import jakarta.persistence.EntityManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import szte.flowboard.dto.request.CreateCOCReportRequestDto;
import szte.flowboard.dto.request.CreateEmployeeMatrixReportRequestDto;
import szte.flowboard.dto.request.CreateProjectActivityReportRequestDto;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.entity.ReportEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.enums.UserRole;
import szte.flowboard.repository.*;
import szte.flowboard.service.report.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing reports.
 * Handles report generation (COC, Employee Matrix, Project Activity), storage in S3,
 * retrieval, renaming, and deletion. Reports are generated as PDFs and stored in AWS S3.
 */
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;
    private final S3Service s3Service;
    private final EmployeeMatrixReportGenerator employeeMatrixReportGenerator;
    private final ProjectActivityReportGenerator projectActivityReportGenerator;
    private final COCReportGenerator cocReportGenerator;

    public ReportService(
            ReportRepository reportRepository,
            UserService userService,
            ProjectUserRepository projectUserRepository,
            ProjectRepository projectRepository,
            EntityManager entityManager,
            S3Service s3Service,
            EmployeeMatrixReportGenerator employeeMatrixReportGenerator,
            ProjectActivityReportGenerator projectActivityReportGenerator,
            COCReportGenerator cocReportGenerator) {
        this.reportRepository = reportRepository;
        this.userService = userService;
        this.projectUserRepository = projectUserRepository;
        this.projectRepository = projectRepository;
        this.entityManager = entityManager;
        this.s3Service = s3Service;
        this.employeeMatrixReportGenerator = employeeMatrixReportGenerator;
        this.projectActivityReportGenerator = projectActivityReportGenerator;
        this.cocReportGenerator = cocReportGenerator;
    }

    /**
     * Creates an employee matrix report showing time logged by employees across projects.
     *
     * @param report the employee matrix report request containing date range
     * @param authentication the authentication object containing the current user's information
     * @return the UUID of the created report, or null if user not found or generation fails
     * @throws IOException if an I/O error occurs during report generation
     */
    public UUID createEmployeeMatrix(CreateEmployeeMatrixReportRequestDto report, Authentication authentication)
            throws IOException {
        var user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            return null;
        }

        var data = employeeMatrixReportGenerator.generate(report, user.get().getId());

        if (data == null) {
            return null;
        }

        return persistReport(null, user.get().getId(), report.getStartDate(), report.getEndDate(), "munkavallaloi_matrix", data);
    }

    /**
     * Creates a project activity report showing task activity for a specific project.
     *
     * @param report the project activity report request containing project and date range
     * @param authentication the authentication object containing the current user's information
     * @return the UUID of the created report, or null if user not found, no access, or generation fails
     * @throws IOException if an I/O error occurs during report generation
     */
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

        var data = projectActivityReportGenerator.generate(report, project);

        return persistReport(
                project.getId(),
                user.getId(),
                report.getStartDate(),
                report.getEndDate(),
                "projekt_aktivitas",
                data);
    }

    /**
     * Creates a Certificate of Completion (COC) report for a project.
     * Supports both time-based and story-point-based projects.
     *
     * @param report the COC report request containing project and date range
     * @param authentication the authentication object containing the current user's information
     * @return the UUID of the created report, or null if user not found, no access, or generation fails
     * @throws IOException if an I/O error occurs during report generation
     */
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

        var data = cocReportGenerator.generate(report, project);

        return persistReport(project.getId(), user.getId(), report.getStartDate(), report.getEndDate(), "teljesitesi_igazolas", data);
    }

    /**
     * Retrieves all reports created by the current user.
     *
     * @param authentication the authentication object containing the current user's information
     * @return a list of report entities for the user, or an empty list if user not found
     */
    public List<ReportEntity> findAllByUser(Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            return List.of();
        }

        return reportRepository.findByUserId(user.get().getId());
    }

    /**
     * Generates a presigned download URL for a report from S3.
     * The URL is valid for 5 minutes.
     *
     * @param reportId the unique identifier of the report
     * @param authentication the authentication object containing the current user's information
     * @return a presigned URL for downloading the report, or null if user not found or report not accessible
     */
    public URL getDownloadUrl(UUID reportId, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            return null;
        }

        var report = reportRepository.findByIdAndUserId(reportId, user.get().getId());

        if (report.isEmpty()) {
            return null;
        }

        return s3Service.getDownloadUrl(reportId, report.get().contentDisposition(), "application/pdf");
    }

    /**
     * Deletes a report and its associated file from S3 if the current user owns it.
     *
     * @param reportId the unique identifier of the report to delete
     * @param authentication the authentication object containing the current user's information
     * @return true if the report was deleted successfully, false otherwise
     */
    @Transactional
    public boolean deleteReport(UUID reportId, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        var result = user
                .filter(userEntity -> reportRepository.deleteByIdAndUserId(reportId, userEntity.getId()) == 1)
                .isPresent();

        if (!result) {
            return false;
        }

        return s3Service.deleteReport(reportId);
    }

    /**
     * Renames a report if the current user owns it.
     *
     * @param reportId the unique identifier of the report to rename
     * @param name the new name for the report
     * @param authentication the authentication object containing the current user's information
     * @return true if the report was renamed successfully, false otherwise
     */
    @Transactional
    public boolean renameReport(UUID reportId, String name, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        return user.filter(userEntity -> reportRepository
                .renameReportByIdAndUserId(name, reportId, userEntity.getId()) == 1).isPresent();
    }

    /**
     * Checks if a user has access to a project (REPORTER or MAINTAINER role).
     *
     * @param projectId the unique identifier of the project
     * @param userId the unique identifier of the user
     * @return the project entity if the user has access, null otherwise
     */
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

    /**
     * Persists a report entity and uploads the PDF data to S3.
     *
     * @param projectId the unique identifier of the project (can be null for employee matrix reports)
     * @param userId the unique identifier of the user creating the report
     * @param start the start date of the report period
     * @param end the end date of the report period
     * @param name the name of the report
     * @param data the PDF data to upload to S3
     * @return the UUID of the created report, or null if S3 upload fails
     */
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

        if (!s3Service.uploadReport(entity.getId(), data)) {
            return null;
        }

        return entity.getId();
    }
}
