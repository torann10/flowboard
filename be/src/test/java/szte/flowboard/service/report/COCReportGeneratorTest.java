package szte.flowboard.service.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import szte.flowboard.dto.request.CreateCOCReportRequestDto;
import szte.flowboard.entity.*;
import szte.flowboard.enums.ProjectType;
import szte.flowboard.enums.TaskStatus;
import szte.flowboard.repository.ProjectUserRepository;
import szte.flowboard.repository.TaskRepository;
import szte.flowboard.repository.TimeLogRepository;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class COCReportGeneratorTest {

    @Mock
    private TimeLogRepository timeLogRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectUserRepository projectUserRepository;

    @Mock
    private HTMLGenerator htmlGenerator;

    @Mock
    private PDFGenerator pdfGenerator;

    @InjectMocks
    private COCReportGenerator generator;

    private UUID projectId;
    private UUID userId;
    private ProjectEntity testProject;
    private UserEntity testUser;
    private CreateCOCReportRequestDto request;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        testUser = new UserEntity();
        testUser.setId(userId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        CompanyEntity customer = new CompanyEntity();
        customer.setName("Test Customer");
        customer.setAddress("Test Customer Address");

        CompanyEntity contractor = new CompanyEntity();
        contractor.setName("Test Contractor");
        contractor.setAddress("Test Contractor Address");

        testProject = new ProjectEntity();
        testProject.setId(projectId);
        testProject.setName("Test Project");
        testProject.setType(ProjectType.TIME_BASED);
        testProject.setCustomer(customer);
        testProject.setContractor(contractor);

        request = new CreateCOCReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setDescription("Test Description");
    }

    @Test
    void testGenerate_TimeBased_NoTimeLogs_Success() throws IOException {
        // Given
        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(timeLogRepository.findAllByTaskProjectIdAndLogDateBetween(
            eq(projectId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        when(htmlGenerator.generateFromCOC(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, testProject);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
        verify(timeLogRepository, times(1))
            .findAllByTaskProjectIdAndLogDateBetween(eq(projectId), any(LocalDate.class), any(LocalDate.class));
        verify(htmlGenerator, times(1)).generateFromCOC(any());
    }

    @Test
    void testGenerate_TimeBased_WithTimeLogs_Success() throws IOException {
        // Given
        ProjectUserEntity projectUser = new ProjectUserEntity();
        projectUser.setUser(testUser);
        projectUser.setFee(100.0);

        TaskEntity task = new TaskEntity();
        task.setId(UUID.randomUUID());
        task.setProject(testProject);

        TimeLogEntity timeLog = new TimeLogEntity();
        timeLog.setId(UUID.randomUUID());
        timeLog.setUser(testUser);
        timeLog.setTask(task);
        timeLog.setLoggedTime(Duration.ofHours(8));
        timeLog.setLogDate(LocalDate.now().minusDays(3));

        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(timeLogRepository.findAllByTaskProjectIdAndLogDateBetween(
            eq(projectId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(timeLog));
        when(projectUserRepository.findByUserIdAndProjectId(userId, projectId))
            .thenReturn(Optional.of(projectUser));
        when(htmlGenerator.generateFromCOC(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, testProject);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
        verify(projectUserRepository, times(1)).findByUserIdAndProjectId(userId, projectId);
    }

    @Test
    void testGenerate_TimeBased_MissingProjectUser_Success() throws IOException {
        // Given
        TaskEntity task = new TaskEntity();
        task.setId(UUID.randomUUID());
        task.setProject(testProject);

        TimeLogEntity timeLog = new TimeLogEntity();
        timeLog.setId(UUID.randomUUID());
        timeLog.setUser(testUser);
        timeLog.setTask(task);
        timeLog.setLoggedTime(Duration.ofHours(8));
        timeLog.setLogDate(LocalDate.now().minusDays(3));

        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(timeLogRepository.findAllByTaskProjectIdAndLogDateBetween(
            eq(projectId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(timeLog));
        when(projectUserRepository.findByUserIdAndProjectId(userId, projectId))
            .thenReturn(Optional.empty());
        when(htmlGenerator.generateFromCOC(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, testProject);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
        // TimeLog without ProjectUser should be skipped
    }

    @Test
    void testGenerate_StoryPointBased_NoTasks_Success() throws IOException {
        // Given
        testProject.setType(ProjectType.STORY_POINT_BASED);
        testProject.setStoryPointFee(100.0);

        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(taskRepository.findByProjectIdAndFinishedAtBetween(
            eq(projectId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        when(htmlGenerator.generateFromCOC(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, testProject);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
        verify(taskRepository, times(1))
            .findByProjectIdAndFinishedAtBetween(eq(projectId), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(timeLogRepository, never()).findAllByTaskProjectIdAndLogDateBetween(any(), any(), any());
    }

    @Test
    void testGenerate_StoryPointBased_WithTasks_Success() throws IOException {
        // Given
        testProject.setType(ProjectType.STORY_POINT_BASED);
        testProject.setStoryPointFee(100.0);

        TaskEntity task = new TaskEntity();
        task.setId(UUID.randomUUID());
        task.setName("Test Task");
        task.setProject(testProject);
        task.setStatus(TaskStatus.DONE);
        task.setFinishedAt(LocalDateTime.now().minusDays(3));

        StoryPointTimeMappingEntity mapping = new StoryPointTimeMappingEntity();
        mapping.setStoryPoints(5);
        task.setStoryPointMapping(mapping);

        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(taskRepository.findByProjectIdAndFinishedAtBetween(
            eq(projectId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(List.of(task));
        when(htmlGenerator.generateFromCOC(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, testProject);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
        verify(taskRepository, times(1))
            .findByProjectIdAndFinishedAtBetween(eq(projectId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGenerate_StoryPointBased_WithMultipleTasks_Success() throws IOException {
        // Given
        testProject.setType(ProjectType.STORY_POINT_BASED);
        testProject.setStoryPointFee(100.0);

        TaskEntity task1 = new TaskEntity();
        task1.setId(UUID.randomUUID());
        task1.setName("Task 1");
        task1.setProject(testProject);
        task1.setStatus(TaskStatus.DONE);
        task1.setFinishedAt(LocalDateTime.now().minusDays(3));

        StoryPointTimeMappingEntity mapping1 = new StoryPointTimeMappingEntity();
        mapping1.setStoryPoints(3);
        task1.setStoryPointMapping(mapping1);

        TaskEntity task2 = new TaskEntity();
        task2.setId(UUID.randomUUID());
        task2.setName("Task 2");
        task2.setProject(testProject);
        task2.setStatus(TaskStatus.DONE);
        task2.setFinishedAt(LocalDateTime.now().minusDays(2));

        StoryPointTimeMappingEntity mapping2 = new StoryPointTimeMappingEntity();
        mapping2.setStoryPoints(5);
        task2.setStoryPointMapping(mapping2);

        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(taskRepository.findByProjectIdAndFinishedAtBetween(
            eq(projectId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(List.of(task1, task2));
        when(htmlGenerator.generateFromCOC(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, testProject);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
    }
}
