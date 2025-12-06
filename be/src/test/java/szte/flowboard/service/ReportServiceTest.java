package szte.flowboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import szte.flowboard.dto.request.CreateCOCReportRequestDto;
import szte.flowboard.dto.request.CreateEmployeeMatrixReportRequestDto;
import szte.flowboard.dto.request.CreateProjectActivityReportRequestDto;
import szte.flowboard.entity.*;
import szte.flowboard.enums.ProjectType;
import szte.flowboard.enums.UserRole;
import szte.flowboard.repository.*;

import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProjectUserRepository projectUserRepository;

    @Mock
    private TimeLogRepository timeLogRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private S3Service s3Service;

    @Mock
    private szte.flowboard.service.report.EmployeeMatrixReportGenerator employeeMatrixReportGenerator;

    @Mock
    private szte.flowboard.service.report.ProjectActivityReportGenerator projectActivityReportGenerator;

    @Mock
    private szte.flowboard.service.report.COCReportGenerator cocReportGenerator;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private ReportService reportService;

    private UserEntity testUser;
    private ProjectEntity testProject;
    private ReportEntity testReport;
    private UUID userId;
    private UUID projectId;
    private UUID reportId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        reportId = UUID.randomUUID();

        testUser = new UserEntity();
        testUser.setId(userId);
        testUser.setEmailAddress("test@example.com");

        testProject = new ProjectEntity();
        testProject.setId(projectId);
        testProject.setName("Test Project");
        testProject.setType(ProjectType.TIME_BASED);
        
        CompanyEntity customer = new CompanyEntity();
        customer.setName("Test Customer");
        customer.setAddress("Test Customer Address");
        testProject.setCustomer(customer);
        
        CompanyEntity contractor = new CompanyEntity();
        contractor.setName("Test Contractor");
        contractor.setAddress("Test Contractor Address");
        testProject.setContractor(contractor);

        testReport = new ReportEntity();
        testReport.setId(reportId);
        testReport.setUser(testUser);
        testReport.setProject(testProject);
        testReport.setName("Test Report");
        testReport.setStart(LocalDate.now().minusDays(7));
        testReport.setEnd(LocalDate.now());
    }

    @Test
    void testFindAllByUser_UserNotFound_ReturnsEmptyList() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        List<ReportEntity> result = reportService.findAllByUser(authentication);

        // Then
        assertTrue(result.isEmpty());
        verify(reportRepository, never()).findByUserId(any());
    }

    @Test
    void testFindAllByUser_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(reportRepository.findByUserId(userId)).thenReturn(List.of(testReport));

        // When
        List<ReportEntity> result = reportService.findAllByUser(authentication);

        // Then
        assertEquals(1, result.size());
        assertEquals(testReport.getId(), result.get(0).getId());
        verify(reportRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetDownloadUrl_UserNotFound_ReturnsNull() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        URL result = reportService.getDownloadUrl(reportId, authentication);

        // Then
        assertNull(result);
    }

    @Test
    void testGetDownloadUrl_ReportNotFound_ReturnsNull() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(reportRepository.findByIdAndUserId(reportId, userId)).thenReturn(Optional.empty());

        // When
        URL result = reportService.getDownloadUrl(reportId, authentication);

        // Then
        assertNull(result);
        verify(s3Service, never()).getDownloadUrl(any(), any(), any());
    }

    @Test
    void testGetDownloadUrl_Success() throws Exception {
        // Given
        URL expectedUrl = URI.create("https://example.com/report.pdf").toURL();
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(reportRepository.findByIdAndUserId(reportId, userId)).thenReturn(Optional.of(testReport));
        when(s3Service.getDownloadUrl(reportId, testReport.contentDisposition(), "application/pdf"))
            .thenReturn(expectedUrl);

        // When
        URL result = reportService.getDownloadUrl(reportId, authentication);

        // Then
        assertEquals(expectedUrl, result);
        verify(s3Service, times(1)).getDownloadUrl(reportId, testReport.contentDisposition(), "application/pdf");
    }

    @Test
    void testDeleteReport_UserNotFound_ReturnsFalse() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        boolean result = reportService.deleteReport(reportId, authentication);

        // Then
        assertFalse(result);
        verify(reportRepository, never()).deleteByIdAndUserId(any(), any());
    }

    @Test
    void testDeleteReport_ReportNotFound_ReturnsFalse() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(reportRepository.deleteByIdAndUserId(reportId, userId)).thenReturn(0);

        // When
        boolean result = reportService.deleteReport(reportId, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testDeleteReport_Success_ReturnsTrue() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(reportRepository.deleteByIdAndUserId(reportId, userId)).thenReturn(1);
        when(s3Service.deleteReport(reportId)).thenReturn(true);

        // When
        boolean result = reportService.deleteReport(reportId, authentication);

        // Then
        assertTrue(result);
        verify(reportRepository, times(1)).deleteByIdAndUserId(reportId, userId);
        verify(s3Service, times(1)).deleteReport(reportId);
    }

    @Test
    void testDeleteReport_S3DeleteFails_ReturnsFalse() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(reportRepository.deleteByIdAndUserId(reportId, userId)).thenReturn(1);
        when(s3Service.deleteReport(reportId)).thenReturn(false);

        // When
        boolean result = reportService.deleteReport(reportId, authentication);

        // Then
        assertFalse(result);
        verify(s3Service, times(1)).deleteReport(reportId);
    }

    @Test
    void testRenameReport_UserNotFound_ReturnsFalse() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        boolean result = reportService.renameReport(reportId, "New Name", authentication);

        // Then
        assertFalse(result);
        verify(reportRepository, never()).renameReportByIdAndUserId(any(), any(), any());
    }

    @Test
    void testRenameReport_Success_ReturnsTrue() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(reportRepository.renameReportByIdAndUserId("New Name", reportId, userId)).thenReturn(1);

        // When
        boolean result = reportService.renameReport(reportId, "New Name", authentication);

        // Then
        assertTrue(result);
        verify(reportRepository, times(1)).renameReportByIdAndUserId("New Name", reportId, userId);
    }

    @Test
    void testRenameReport_UpdateFailed_ReturnsFalse() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(reportRepository.renameReportByIdAndUserId("New Name", reportId, userId)).thenReturn(0);

        // When
        boolean result = reportService.renameReport(reportId, "New Name", authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCreateCOC_UserNotFound_ReturnsNull() throws IOException {
        // Given
        CreateCOCReportRequestDto request = new CreateCOCReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setDescription("Test Description");

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        UUID result = reportService.createCOC(request, authentication);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateEmployeeMatrix_UserNotFound_ReturnsNull() throws IOException {
        // Given
        CreateEmployeeMatrixReportRequestDto request = new CreateEmployeeMatrixReportRequestDto();
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        UUID result = reportService.createEmployeeMatrix(request, authentication);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateProjectActivityReport_UserNotFound_ReturnsNull() throws IOException {
        // Given
        CreateProjectActivityReportRequestDto request = new CreateProjectActivityReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        UUID result = reportService.createProjectActivityReport(request, authentication);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateEmployeeMatrix_NoProjects_ReturnsNull() throws IOException {
        // Given
        CreateEmployeeMatrixReportRequestDto request = new CreateEmployeeMatrixReportRequestDto();
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(employeeMatrixReportGenerator.generate(request, userId)).thenReturn(null);

        // When
        UUID result = reportService.createEmployeeMatrix(request, authentication);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateCOC_NoProjectAccess_ReturnsNull() throws IOException {
        // Given
        CreateCOCReportRequestDto request = new CreateCOCReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setDescription("Test Description");

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER))
            .thenReturn(false);
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.MAINTAINER))
            .thenReturn(false);

        // When
        UUID result = reportService.createCOC(request, authentication);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateCOC_ProjectNotFound_ReturnsNull() throws IOException {
        // Given
        CreateCOCReportRequestDto request = new CreateCOCReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setDescription("Test Description");

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When
        UUID result = reportService.createCOC(request, authentication);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateProjectActivityReport_NoProjectAccess_ReturnsNull() throws IOException {
        // Given
        CreateProjectActivityReportRequestDto request = new CreateProjectActivityReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER))
            .thenReturn(false);
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.MAINTAINER))
            .thenReturn(false);

        // When
        UUID result = reportService.createProjectActivityReport(request, authentication);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateProjectActivityReport_ProjectNotFound_ReturnsNull() throws IOException {
        // Given
        CreateProjectActivityReportRequestDto request = new CreateProjectActivityReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When
        UUID result = reportService.createProjectActivityReport(request, authentication);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateCOC_WithReporterRole_Success() throws IOException {
        // Given
        CreateCOCReportRequestDto request = new CreateCOCReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setDescription("Test Description");

        byte[] reportData = new byte[]{1, 2, 3};

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER))
            .thenReturn(true);
        when(cocReportGenerator.generate(request, testProject)).thenReturn(reportData);
        when(entityManager.getReference(ProjectEntity.class, projectId)).thenReturn(testProject);
        when(entityManager.getReference(UserEntity.class, userId)).thenReturn(testUser);
        when(reportRepository.save(any(ReportEntity.class))).thenAnswer(invocation -> {
            ReportEntity report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });
        when(s3Service.uploadReport(reportId, reportData)).thenReturn(true);

        // When
        UUID result = reportService.createCOC(request, authentication);

        // Then
        assertNotNull(result);
        verify(projectRepository, times(1)).findById(projectId);
        verify(cocReportGenerator, times(1)).generate(request, testProject);
        verify(s3Service, times(1)).uploadReport(reportId, reportData);
    }

    @Test
    void testCreateCOC_WithMaintainerRole_Success() throws IOException {
        // Given
        CreateCOCReportRequestDto request = new CreateCOCReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setDescription("Test Description");

        byte[] reportData = new byte[]{1, 2, 3};

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER))
            .thenReturn(false);
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.MAINTAINER))
            .thenReturn(true);
        when(cocReportGenerator.generate(request, testProject)).thenReturn(reportData);
        when(entityManager.getReference(ProjectEntity.class, projectId)).thenReturn(testProject);
        when(entityManager.getReference(UserEntity.class, userId)).thenReturn(testUser);
        when(reportRepository.save(any(ReportEntity.class))).thenAnswer(invocation -> {
            ReportEntity report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });
        when(s3Service.uploadReport(reportId, reportData)).thenReturn(true);

        // When
        UUID result = reportService.createCOC(request, authentication);

        // Then
        assertNotNull(result);
        verify(projectRepository, times(1)).findById(projectId);
        verify(cocReportGenerator, times(1)).generate(request, testProject);
        verify(s3Service, times(1)).uploadReport(reportId, reportData);
    }

    @Test
    void testCreateCOC_StoryPointBased_Success() throws IOException {
        // Given
        testProject.setType(ProjectType.STORY_POINT_BASED);
        testProject.setStoryPointFee(100.0);
        
        CreateCOCReportRequestDto request = new CreateCOCReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setDescription("Test Description");

        byte[] reportData = new byte[]{1, 2, 3};

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER))
            .thenReturn(true);
        when(cocReportGenerator.generate(request, testProject)).thenReturn(reportData);
        when(entityManager.getReference(ProjectEntity.class, projectId)).thenReturn(testProject);
        when(entityManager.getReference(UserEntity.class, userId)).thenReturn(testUser);
        when(reportRepository.save(any(ReportEntity.class))).thenAnswer(invocation -> {
            ReportEntity report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });
        when(s3Service.uploadReport(reportId, reportData)).thenReturn(true);

        // When
        UUID result = reportService.createCOC(request, authentication);

        // Then
        assertNotNull(result);
        verify(cocReportGenerator, times(1)).generate(request, testProject);
    }

    @Test
    void testCreateProjectActivityReport_WithReporterRole_Success() throws IOException {
        // Given
        CreateProjectActivityReportRequestDto request = new CreateProjectActivityReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());

        byte[] reportData = new byte[]{1, 2, 3};

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER))
            .thenReturn(true);
        when(projectActivityReportGenerator.generate(request, testProject)).thenReturn(reportData);
        when(entityManager.getReference(ProjectEntity.class, projectId)).thenReturn(testProject);
        when(entityManager.getReference(UserEntity.class, userId)).thenReturn(testUser);
        when(reportRepository.save(any(ReportEntity.class))).thenAnswer(invocation -> {
            ReportEntity report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });
        when(s3Service.uploadReport(reportId, reportData)).thenReturn(true);

        // When
        UUID result = reportService.createProjectActivityReport(request, authentication);

        // Then
        assertNotNull(result);
        verify(projectRepository, times(1)).findById(projectId);
        verify(projectActivityReportGenerator, times(1)).generate(request, testProject);
        verify(s3Service, times(1)).uploadReport(reportId, reportData);
    }

    @Test
    void testCreateProjectActivityReport_WithTasks_Success() throws IOException {
        // Given
        CreateProjectActivityReportRequestDto request = new CreateProjectActivityReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());

        byte[] reportData = new byte[]{1, 2, 3};

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER))
            .thenReturn(true);
        when(projectActivityReportGenerator.generate(request, testProject)).thenReturn(reportData);
        when(entityManager.getReference(ProjectEntity.class, projectId)).thenReturn(testProject);
        when(entityManager.getReference(UserEntity.class, userId)).thenReturn(testUser);
        when(reportRepository.save(any(ReportEntity.class))).thenAnswer(invocation -> {
            ReportEntity report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });
        when(s3Service.uploadReport(reportId, reportData)).thenReturn(true);

        // When
        UUID result = reportService.createProjectActivityReport(request, authentication);

        // Then
        assertNotNull(result);
        verify(projectActivityReportGenerator, times(1)).generate(request, testProject);
    }

    @Test
    void testCreateEmployeeMatrix_WithTimeLogs_Success() throws IOException {
        // Given
        CreateEmployeeMatrixReportRequestDto request = new CreateEmployeeMatrixReportRequestDto();
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());

        byte[] reportData = new byte[]{1, 2, 3};

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(employeeMatrixReportGenerator.generate(request, userId)).thenReturn(reportData);
        when(entityManager.getReference(UserEntity.class, userId)).thenReturn(testUser);
        when(reportRepository.save(any(ReportEntity.class))).thenAnswer(invocation -> {
            ReportEntity report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });
        when(s3Service.uploadReport(reportId, reportData)).thenReturn(true);

        // When
        UUID result = reportService.createEmployeeMatrix(request, authentication);

        // Then
        assertNotNull(result);
        verify(employeeMatrixReportGenerator, times(1)).generate(request, userId);
        verify(s3Service, times(1)).uploadReport(reportId, reportData);
    }

    @Test
    void testCreateEmployeeMatrix_WithEmptyTimeLogs_Success() throws IOException {
        // Given
        CreateEmployeeMatrixReportRequestDto request = new CreateEmployeeMatrixReportRequestDto();
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());

        byte[] reportData = new byte[]{1, 2, 3};

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(employeeMatrixReportGenerator.generate(request, userId)).thenReturn(reportData);
        when(entityManager.getReference(UserEntity.class, userId)).thenReturn(testUser);
        when(reportRepository.save(any(ReportEntity.class))).thenAnswer(invocation -> {
            ReportEntity report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });
        when(s3Service.uploadReport(reportId, reportData)).thenReturn(true);

        // When
        UUID result = reportService.createEmployeeMatrix(request, authentication);

        // Then
        assertNotNull(result);
        verify(employeeMatrixReportGenerator, times(1)).generate(request, userId);
    }

    @Test
    void testCreateCOC_TimeBased_WithTimeLogs_Success() throws IOException {
        // Given
        CreateCOCReportRequestDto request = new CreateCOCReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setDescription("Test Description");

        byte[] reportData = new byte[]{1, 2, 3};

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER))
            .thenReturn(true);
        when(cocReportGenerator.generate(request, testProject)).thenReturn(reportData);
        when(entityManager.getReference(ProjectEntity.class, projectId)).thenReturn(testProject);
        when(entityManager.getReference(UserEntity.class, userId)).thenReturn(testUser);
        when(reportRepository.save(any(ReportEntity.class))).thenAnswer(invocation -> {
            ReportEntity report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });
        when(s3Service.uploadReport(reportId, reportData)).thenReturn(true);

        // When
        UUID result = reportService.createCOC(request, authentication);

        // Then
        assertNotNull(result);
        verify(cocReportGenerator, times(1)).generate(request, testProject);
        verify(s3Service, times(1)).uploadReport(reportId, reportData);
    }

    @Test
    void testCreateCOC_TimeBased_WithMissingProjectUser_Success() throws IOException {
        // Given
        CreateCOCReportRequestDto request = new CreateCOCReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setDescription("Test Description");

        byte[] reportData = new byte[]{1, 2, 3};

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER))
            .thenReturn(true);
        when(cocReportGenerator.generate(request, testProject)).thenReturn(reportData);
        when(entityManager.getReference(ProjectEntity.class, projectId)).thenReturn(testProject);
        when(entityManager.getReference(UserEntity.class, userId)).thenReturn(testUser);
        when(reportRepository.save(any(ReportEntity.class))).thenAnswer(invocation -> {
            ReportEntity report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });
        when(s3Service.uploadReport(reportId, reportData)).thenReturn(true);

        // When
        UUID result = reportService.createCOC(request, authentication);

        // Then
        assertNotNull(result);
        verify(cocReportGenerator, times(1)).generate(request, testProject);
    }

    @Test
    void testCreateCOC_StoryPointBased_WithTasks_Success() throws IOException {
        // Given
        testProject.setType(ProjectType.STORY_POINT_BASED);
        testProject.setStoryPointFee(100.0);
        
        CreateCOCReportRequestDto request = new CreateCOCReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setDescription("Test Description");

        byte[] reportData = new byte[]{1, 2, 3};

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER))
            .thenReturn(true);
        when(cocReportGenerator.generate(request, testProject)).thenReturn(reportData);
        when(entityManager.getReference(ProjectEntity.class, projectId)).thenReturn(testProject);
        when(entityManager.getReference(UserEntity.class, userId)).thenReturn(testUser);
        when(reportRepository.save(any(ReportEntity.class))).thenAnswer(invocation -> {
            ReportEntity report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });
        when(s3Service.uploadReport(reportId, reportData)).thenReturn(true);

        // When
        UUID result = reportService.createCOC(request, authentication);

        // Then
        assertNotNull(result);
        verify(cocReportGenerator, times(1)).generate(request, testProject);
    }

    @Test
    void testCreateCOC_S3UploadFails_ReturnsNull() throws IOException {
        // Given
        CreateCOCReportRequestDto request = new CreateCOCReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setDescription("Test Description");

        byte[] reportData = new byte[]{1, 2, 3};

        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.REPORTER))
            .thenReturn(true);
        when(cocReportGenerator.generate(request, testProject)).thenReturn(reportData);
        when(entityManager.getReference(ProjectEntity.class, projectId)).thenReturn(testProject);
        when(entityManager.getReference(UserEntity.class, userId)).thenReturn(testUser);
        when(reportRepository.save(any(ReportEntity.class))).thenAnswer(invocation -> {
            ReportEntity report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });
        when(s3Service.uploadReport(reportId, reportData)).thenReturn(false);

        // When
        UUID result = reportService.createCOC(request, authentication);

        // Then
        assertNull(result);
        verify(s3Service, times(1)).uploadReport(reportId, reportData);
    }
}



