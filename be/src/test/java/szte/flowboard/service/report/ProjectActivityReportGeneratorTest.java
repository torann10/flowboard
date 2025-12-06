package szte.flowboard.service.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import szte.flowboard.dto.request.CreateProjectActivityReportRequestDto;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.entity.StoryPointTimeMappingEntity;
import szte.flowboard.entity.TaskEntity;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.enums.TaskStatus;
import szte.flowboard.repository.TaskRepository;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectActivityReportGeneratorTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private HTMLGenerator htmlGenerator;

    @Mock
    private PDFGenerator pdfGenerator;

    @InjectMocks
    private ProjectActivityReportGenerator generator;

    private UUID projectId;
    private ProjectEntity testProject;
    private CreateProjectActivityReportRequestDto request;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();

        testProject = new ProjectEntity();
        testProject.setId(projectId);
        testProject.setName("Test Project");

        request = new CreateProjectActivityReportRequestDto();
        request.setProjectId(projectId);
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
    }

    @Test
    void testGenerate_NoTasks_Success() throws IOException {
        // Given
        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(taskRepository.findByProjectIdAndFinishedAtBetween(
            eq(projectId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        when(htmlGenerator.generateFromProjectActivity(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, testProject);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
        verify(htmlGenerator, times(1)).generateFromProjectActivity(any());
        verify(pdfGenerator, times(1)).generatePdf(html);
    }

    @Test
    void testGenerate_WithTasks_Success() throws IOException {
        // Given
        TaskEntity task = new TaskEntity();
        task.setId(UUID.randomUUID());
        task.setName("Test Task");
        task.setProject(testProject);
        task.setStatus(TaskStatus.DONE);
        task.setFinishedAt(LocalDateTime.now().minusDays(3));
        task.setTimeLogs(new ArrayList<>());

        StoryPointTimeMappingEntity mapping = new StoryPointTimeMappingEntity();
        mapping.setStoryPoints(5);
        mapping.setTimeValue(Duration.ofHours(10));
        task.setStoryPointMapping(mapping);

        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(taskRepository.findByProjectIdAndFinishedAtBetween(
            eq(projectId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(List.of(task));
        when(htmlGenerator.generateFromProjectActivity(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, testProject);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
    }

    @Test
    void testGenerate_WithTasksAndTimeLogs_Success() throws IOException {
        // Given
        TaskEntity task = new TaskEntity();
        task.setId(UUID.randomUUID());
        task.setName("Test Task");
        task.setProject(testProject);
        task.setStatus(TaskStatus.DONE);
        task.setFinishedAt(LocalDateTime.now().minusDays(3));

        TimeLogEntity timeLog = new TimeLogEntity();
        timeLog.setId(UUID.randomUUID());
        timeLog.setLoggedTime(Duration.ofHours(8));
        task.setTimeLogs(List.of(timeLog));

        StoryPointTimeMappingEntity mapping = new StoryPointTimeMappingEntity();
        mapping.setStoryPoints(5);
        mapping.setTimeValue(Duration.ofHours(10));
        task.setStoryPointMapping(mapping);

        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(taskRepository.findByProjectIdAndFinishedAtBetween(
            eq(projectId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(List.of(task));
        when(htmlGenerator.generateFromProjectActivity(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, testProject);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
    }

    @Test
    void testGenerate_WithMultipleTasks_Success() throws IOException {
        // Given
        TaskEntity task1 = new TaskEntity();
        task1.setId(UUID.randomUUID());
        task1.setName("Task 1");
        task1.setProject(testProject);
        task1.setStatus(TaskStatus.DONE);
        task1.setFinishedAt(LocalDateTime.now().minusDays(3));
        task1.setTimeLogs(new ArrayList<>());

        StoryPointTimeMappingEntity mapping1 = new StoryPointTimeMappingEntity();
        mapping1.setStoryPoints(3);
        mapping1.setTimeValue(Duration.ofHours(6));
        task1.setStoryPointMapping(mapping1);

        TaskEntity task2 = new TaskEntity();
        task2.setId(UUID.randomUUID());
        task2.setName("Task 2");
        task2.setProject(testProject);
        task2.setStatus(TaskStatus.DONE);
        task2.setFinishedAt(LocalDateTime.now().minusDays(2));
        task2.setTimeLogs(new ArrayList<>());

        StoryPointTimeMappingEntity mapping2 = new StoryPointTimeMappingEntity();
        mapping2.setStoryPoints(5);
        mapping2.setTimeValue(Duration.ofHours(10));
        task2.setStoryPointMapping(mapping2);

        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(taskRepository.findByProjectIdAndFinishedAtBetween(
            eq(projectId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(List.of(task1, task2));
        when(htmlGenerator.generateFromProjectActivity(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, testProject);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
    }
}

