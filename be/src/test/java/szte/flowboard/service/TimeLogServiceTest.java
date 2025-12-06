package szte.flowboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.entity.TaskEntity;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.ProjectUserRepository;
import szte.flowboard.repository.TaskRepository;
import szte.flowboard.repository.TimeLogRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeLogServiceTest {

    @Mock
    private TimeLogRepository timeLogRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProjectUserRepository projectUserRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private TimeLogService timeLogService;

    private UserEntity testUser;
    private ProjectEntity testProject;
    private TaskEntity testTask;
    private TimeLogEntity testTimeLog;
    private UUID userId;
    private UUID projectId;
    private UUID taskId;
    private UUID timeLogId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        timeLogId = UUID.randomUUID();

        testUser = new UserEntity();
        testUser.setId(userId);
        testUser.setEmailAddress("test@example.com");

        testProject = new ProjectEntity();
        testProject.setId(projectId);

        testTask = new TaskEntity();
        testTask.setId(taskId);
        testTask.setProject(testProject);
        testTask.setTimeLogs(new ArrayList<>());

        testTimeLog = new TimeLogEntity();
        testTimeLog.setId(timeLogId);
        testTimeLog.setTask(testTask);
        testTimeLog.setUser(testUser);
        testTimeLog.setLoggedTime(Duration.ofHours(2));
        testTimeLog.setLogDate(LocalDate.now());
        testTimeLog.setIsBillable(true);
    }

    @Test
    void testCreate_UserNotFound_ReturnsNull() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        TimeLogEntity result = timeLogService.create(testTimeLog, authentication);

        // Then
        assertNull(result);
        verify(timeLogRepository, never()).save(any());
    }

    @Test
    void testCreate_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(timeLogRepository.save(any(TimeLogEntity.class))).thenAnswer(invocation -> {
            TimeLogEntity tl = invocation.getArgument(0);
            tl.setUser(testUser);
            return tl;
        });

        // When
        TimeLogEntity result = timeLogService.create(testTimeLog, authentication);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUser().getId());
        verify(timeLogRepository, times(1)).save(argThat(tl -> 
            tl.getUser().getId().equals(userId)
        ));
    }

    @Test
    void testFindAllByUser_UserNotFound_ReturnsEmptyList() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        List<TimeLogEntity> result = timeLogService.findAllByUser(authentication);

        // Then
        assertTrue(result.isEmpty());
        verify(timeLogRepository, never()).findByUserId(any());
    }

    @Test
    void testFindAllByUser_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(timeLogRepository.findByUserId(userId)).thenReturn(List.of(testTimeLog));

        // When
        List<TimeLogEntity> result = timeLogService.findAllByUser(authentication);

        // Then
        assertEquals(1, result.size());
        assertEquals(testTimeLog.getId(), result.get(0).getId());
    }

    @Test
    void testFindByIdAndUser_UserNotFound_ReturnsEmpty() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        Optional<TimeLogEntity> result = timeLogService.findByIdAndUser(timeLogId, authentication);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByIdAndUser_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(timeLogRepository.findByIdAndUserId(timeLogId, userId)).thenReturn(Optional.of(testTimeLog));

        // When
        Optional<TimeLogEntity> result = timeLogService.findByIdAndUser(timeLogId, authentication);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testTimeLog.getId(), result.get().getId());
    }

    @Test
    void testFindAllByTaskId_UserNotFound_ReturnsEmptyList() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        List<TimeLogEntity> result = timeLogService.findAllByTaskId(taskId, authentication);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllByTaskId_TaskNotFound_ReturnsEmptyList() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // When
        List<TimeLogEntity> result = timeLogService.findAllByTaskId(taskId, authentication);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllByTaskId_NoAccess_ReturnsEmptyList() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(projectUserRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(false);

        // When
        List<TimeLogEntity> result = timeLogService.findAllByTaskId(taskId, authentication);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllByTaskId_Success() {
        // Given
        testTask.getTimeLogs().add(testTimeLog);
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(projectUserRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(true);

        // When
        List<TimeLogEntity> result = timeLogService.findAllByTaskId(taskId, authentication);

        // Then
        assertEquals(1, result.size());
        assertEquals(testTimeLog.getId(), result.get(0).getId());
    }

    @Test
    void testUpdate_UserNotFound_ThrowsException() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            timeLogService.update(testTimeLog, authentication);
        });
    }

    @Test
    void testUpdate_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(timeLogRepository.save(any(TimeLogEntity.class))).thenReturn(testTimeLog);

        // When
        TimeLogEntity result = timeLogService.update(testTimeLog, authentication);

        // Then
        assertNotNull(result);
        verify(timeLogRepository, times(1)).save(argThat(tl -> 
            tl.getUser().getId().equals(userId)
        ));
    }

    @Test
    void testDelete_Success() {
        // Given
        doNothing().when(timeLogRepository).deleteById(timeLogId);

        // When
        timeLogService.delete(timeLogId);

        // Then
        verify(timeLogRepository, times(1)).deleteById(timeLogId);
    }

    @Test
    void testExistsByIdAndUser_UserNotFound_ReturnsFalse() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        boolean result = timeLogService.existsByIdAndUser(timeLogId, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testExistsByIdAndUser_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(timeLogRepository.existsByIdAndUserId(timeLogId, userId)).thenReturn(true);

        // When
        boolean result = timeLogService.existsByIdAndUser(timeLogId, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testExistsByIdAndUser_NotFound_ReturnsFalse() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(timeLogRepository.existsByIdAndUserId(timeLogId, userId)).thenReturn(false);

        // When
        boolean result = timeLogService.existsByIdAndUser(timeLogId, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testFindAllByUser_EmptyList() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(timeLogRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // When
        List<TimeLogEntity> result = timeLogService.findAllByUser(authentication);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllByTaskId_EmptyTimeLogs() {
        // Given
        testTask.setTimeLogs(Collections.emptyList());
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(projectUserRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(true);

        // When
        List<TimeLogEntity> result = timeLogService.findAllByTaskId(taskId, authentication);

        // Then
        assertTrue(result.isEmpty());
    }
}



