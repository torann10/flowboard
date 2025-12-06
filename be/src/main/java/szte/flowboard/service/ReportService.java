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

        return s3Service.getDownloadUrl(reportId, report.get().contentDisposition(), "application/pdf");
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

        return s3Service.deleteReport(reportId);
    }

    @Transactional
    public boolean renameReport(UUID reportId, String name, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        return user.filter(userEntity -> reportRepository
                .renameReportByIdAndUserId(name, reportId, userEntity.getId()) == 1).isPresent();
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
