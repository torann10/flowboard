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
import szte.flowboard.entity.UserEntity;
import szte.flowboard.enums.TaskStatus;
import szte.flowboard.repository.ProjectRepository;
import szte.flowboard.repository.TaskRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private TaskService taskService;

    private UserEntity testUser;
    private ProjectEntity testProject;
    private TaskEntity testTask;
    private UUID userId;
    private UUID projectId;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        testUser = new UserEntity();
        testUser.setId(userId);
        testUser.setEmailAddress("test@example.com");

        testProject = new ProjectEntity();
        testProject.setId(projectId);
        testProject.setName("Test Project");

        testTask = new TaskEntity();
        testTask.setId(taskId);
        testTask.setName("Test Task");
        testTask.setStatus(TaskStatus.OPEN);
        testTask.setProject(testProject);
    }

    @Test
    void testCreate_UserNotFound_ReturnsNull() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        TaskEntity result = taskService.create(testTask, authentication);

        // Then
        assertNull(result);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void testCreate_NoAccess_ReturnsNull() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.existsByIdAndProjectUsersUserId(projectId, userId)).thenReturn(false);

        // When
        TaskEntity result = taskService.create(testTask, authentication);

        // Then
        assertNull(result);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void testCreate_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.existsByIdAndProjectUsersUserId(projectId, userId)).thenReturn(true);
        when(taskRepository.save(testTask)).thenReturn(testTask);

        // When
        TaskEntity result = taskService.create(testTask, authentication);

        // Then
        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void testFindAllByUser_UserNotFound_ReturnsEmptyList() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        List<TaskEntity> result = taskService.findAllByUser(authentication);

        // Then
        assertTrue(result.isEmpty());
        verify(taskRepository, never()).findByProjectProjectUsersUserId(any());
    }

    @Test
    void testFindAllByUser_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(taskRepository.findByProjectProjectUsersUserId(userId)).thenReturn(List.of(testTask));

        // When
        List<TaskEntity> result = taskService.findAllByUser(authentication);

        // Then
        assertEquals(1, result.size());
        assertEquals(testTask.getId(), result.get(0).getId());
    }

    @Test
    void testFindByIdAndUser_UserNotFound_ReturnsEmpty() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        Optional<TaskEntity> result = taskService.findByIdAndUser(taskId, authentication);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByIdAndUser_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(taskRepository.findByIdAndProjectProjectUsersUserId(taskId, userId)).thenReturn(Optional.of(testTask));

        // When
        Optional<TaskEntity> result = taskService.findByIdAndUser(taskId, authentication);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testTask.getId(), result.get().getId());
    }

    @Test
    void testExistsById_UserNotFound_ReturnsFalse() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        boolean result = taskService.existsById(taskId, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testExistsById_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(taskRepository.existsByIdAndProjectProjectUsersUserId(taskId, userId)).thenReturn(true);

        // When
        boolean result = taskService.existsById(taskId, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testUpdate_TaskNotFound_ReturnsNull() {
        // Given
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // When
        TaskEntity result = taskService.update(testTask);

        // Then
        assertNull(result);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void testUpdate_Success() {
        // Given
        TaskEntity existingTask = new TaskEntity();
        existingTask.setId(taskId);
        existingTask.setProject(testProject);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(testTask);

        // When
        TaskEntity result = taskService.update(testTask);

        // Then
        assertNotNull(result);
        verify(taskRepository, times(1)).save(argThat(task -> 
            task.getProject().getId().equals(projectId)
        ));
    }

    @Test
    void testDelete_Success() {
        // Given
        doNothing().when(taskRepository).deleteById(taskId);

        // When
        taskService.delete(taskId);

        // Then
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void testFindAllByProject_Success() {
        // Given
        when(taskRepository.findByProjectId(projectId)).thenReturn(List.of(testTask));

        // When
        List<TaskEntity> result = taskService.findAllByProject(projectId);

        // Then
        assertEquals(1, result.size());
        assertEquals(testTask.getId(), result.get(0).getId());
    }

    @Test
    void testFindAllByProject_EmptyList() {
        // Given
        when(taskRepository.findByProjectId(projectId)).thenReturn(Collections.emptyList());

        // When
        List<TaskEntity> result = taskService.findAllByProject(projectId);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllByUser_EmptyList() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(taskRepository.findByProjectProjectUsersUserId(userId)).thenReturn(Collections.emptyList());

        // When
        List<TaskEntity> result = taskService.findAllByUser(authentication);

        // Then
        assertTrue(result.isEmpty());
    }
}



