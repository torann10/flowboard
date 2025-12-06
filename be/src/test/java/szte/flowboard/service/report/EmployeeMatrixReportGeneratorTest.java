package szte.flowboard.service.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import szte.flowboard.dto.request.CreateEmployeeMatrixReportRequestDto;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.entity.TaskEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.enums.UserRole;
import szte.flowboard.repository.ProjectRepository;
import szte.flowboard.repository.TimeLogRepository;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeMatrixReportGeneratorTest {

    @Mock
    private TimeLogRepository timeLogRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private HTMLGenerator htmlGenerator;

    @Mock
    private PDFGenerator pdfGenerator;

    @InjectMocks
    private EmployeeMatrixReportGenerator generator;

    private UUID userId;
    private UUID projectId;
    private ProjectEntity testProject;
    private UserEntity testUser;
    private CreateEmployeeMatrixReportRequestDto request;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        testUser = new UserEntity();
        testUser.setId(userId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testProject = new ProjectEntity();
        testProject.setId(projectId);
        testProject.setName("Test Project");

        request = new CreateEmployeeMatrixReportRequestDto();
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
    }

    @Test
    void testGenerate_NoProjects_ReturnsNull() throws IOException {
        // Given
        when(projectRepository.findAllByProjectUsersUserIdAndProjectUsersRole(userId, UserRole.MAINTAINER))
            .thenReturn(Collections.emptyList());

        // When
        byte[] result = generator.generate(request, userId);

        // Then
        assertNull(result);
        verify(projectRepository, times(1))
            .findAllByProjectUsersUserIdAndProjectUsersRole(userId, UserRole.MAINTAINER);
        verify(timeLogRepository, never()).findByTaskProjectIdInAndLogDateBetween(any(), any(), any());
    }

    @Test
    void testGenerate_WithProjectsAndTimeLogs_Success() throws IOException {
        // Given
        TaskEntity task = new TaskEntity();
        task.setId(UUID.randomUUID());
        task.setProject(testProject);

        TimeLogEntity timeLog = new TimeLogEntity();
        timeLog.setId(UUID.randomUUID());
        timeLog.setUser(testUser);
        timeLog.setTask(task);
        timeLog.setLoggedTime(Duration.ofHours(5));
        timeLog.setLogDate(LocalDate.now().minusDays(3));

        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(projectRepository.findAllByProjectUsersUserIdAndProjectUsersRole(userId, UserRole.MAINTAINER))
            .thenReturn(List.of(testProject));
        when(timeLogRepository.findByTaskProjectIdInAndLogDateBetween(any(Set.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(timeLog));
        when(htmlGenerator.generateFromMatrix(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, userId);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
        verify(htmlGenerator, times(1)).generateFromMatrix(any());
        verify(pdfGenerator, times(1)).generatePdf(html);
    }

    @Test
    void testGenerate_WithProjectsButNoTimeLogs_Success() throws IOException {
        // Given
        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(projectRepository.findAllByProjectUsersUserIdAndProjectUsersRole(userId, UserRole.MAINTAINER))
            .thenReturn(List.of(testProject));
        when(timeLogRepository.findByTaskProjectIdInAndLogDateBetween(any(Set.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        when(htmlGenerator.generateFromMatrix(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, userId);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
    }

    @Test
    void testGenerate_WithMultipleUsers_Success() throws IOException {
        // Given
        UserEntity user2 = new UserEntity();
        user2.setId(UUID.randomUUID());
        user2.setFirstName("Jane");
        user2.setLastName("Smith");

        TaskEntity task = new TaskEntity();
        task.setId(UUID.randomUUID());
        task.setProject(testProject);

        TimeLogEntity timeLog1 = new TimeLogEntity();
        timeLog1.setId(UUID.randomUUID());
        timeLog1.setUser(testUser);
        timeLog1.setTask(task);
        timeLog1.setLoggedTime(Duration.ofHours(3));
        timeLog1.setLogDate(LocalDate.now().minusDays(2));

        TimeLogEntity timeLog2 = new TimeLogEntity();
        timeLog2.setId(UUID.randomUUID());
        timeLog2.setUser(user2);
        timeLog2.setTask(task);
        timeLog2.setLoggedTime(Duration.ofHours(4));
        timeLog2.setLogDate(LocalDate.now().minusDays(1));

        String html = "<html>Test HTML</html>";
        byte[] pdfData = new byte[]{1, 2, 3};

        when(projectRepository.findAllByProjectUsersUserIdAndProjectUsersRole(userId, UserRole.MAINTAINER))
            .thenReturn(List.of(testProject));
        when(timeLogRepository.findByTaskProjectIdInAndLogDateBetween(any(Set.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(timeLog1, timeLog2));
        when(htmlGenerator.generateFromMatrix(any())).thenReturn(html);
        when(pdfGenerator.generatePdf(html)).thenReturn(pdfData);

        // When
        byte[] result = generator.generate(request, userId);

        // Then
        assertNotNull(result);
        assertEquals(pdfData, result);
    }
}

