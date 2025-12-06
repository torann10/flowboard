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
import szte.flowboard.entity.ProjectUserEntity;
import szte.flowboard.entity.StoryPointTimeMappingEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.enums.ProjectStatus;
import szte.flowboard.enums.ProjectType;
import szte.flowboard.enums.UserRole;
import szte.flowboard.repository.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectUserRepository projectUserRepository;

    @Mock
    private UserService userService;

    @Mock
    private StoryPointTimeMappingRepository storyPointTimeMappingRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private ProjectService projectService;

    private UserEntity testUser;
    private ProjectEntity testProject;
    private UUID userId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        testUser = new UserEntity();
        testUser.setId(userId);
        testUser.setEmailAddress("test@example.com");

        testProject = new ProjectEntity();
        testProject.setId(projectId);
        testProject.setName("Test Project");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setType(ProjectType.TIME_BASED);
    }

    @Test
    void testCreate_UserNotFound_ThrowsException() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            projectService.create(testProject, authentication);
        });

        verify(projectRepository, never()).save(any());
        verify(projectUserRepository, never()).save(any());
    }

    @Test
    void testCreate_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(ProjectEntity.class))).thenReturn(testProject);
        when(projectUserRepository.save(any(ProjectUserEntity.class))).thenAnswer(invocation -> {
            ProjectUserEntity pu = invocation.getArgument(0);
            pu.setId(UUID.randomUUID());
            return pu;
        });

        // When
        ProjectEntity result = projectService.create(testProject, authentication);

        // Then
        assertNotNull(result);
        assertEquals(testProject.getId(), result.getId());
        verify(projectRepository, times(1)).save(testProject);
        verify(projectUserRepository, times(1)).save(argThat(pu -> 
            pu.getUser().getId().equals(userId) && 
            pu.getProject().getId().equals(projectId) &&
            pu.getRole() == UserRole.MAINTAINER
        ));
    }

    @Test
    void testFindAllByUser_UserNotFound_ReturnsEmptyList() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        List<ProjectEntity> result = projectService.findAllByUser(authentication);

        // Then
        assertTrue(result.isEmpty());
        verify(projectUserRepository, never()).findByUserId(any());
    }

    @Test
    void testFindAllByUser_Success() {
        // Given
        ProjectUserEntity projectUser = new ProjectUserEntity();
        projectUser.setProject(testProject);
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectUserRepository.findByUserId(userId)).thenReturn(List.of(projectUser));

        // When
        List<ProjectEntity> result = projectService.findAllByUser(authentication);

        // Then
        assertEquals(1, result.size());
        assertEquals(testProject.getId(), result.get(0).getId());
        verify(projectUserRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testFindByIdAndUser_UserNotFound_ReturnsEmpty() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        Optional<ProjectEntity> result = projectService.findByIdAndUser(projectId, authentication);

        // Then
        assertTrue(result.isEmpty());
        verify(projectRepository, never()).findById(any());
    }

    @Test
    void testFindByIdAndUser_NoAccess_ReturnsEmpty() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectUserRepository.findByUserIdAndProjectId(userId, projectId)).thenReturn(Optional.empty());

        // When
        Optional<ProjectEntity> result = projectService.findByIdAndUser(projectId, authentication);

        // Then
        assertTrue(result.isEmpty());
        verify(projectRepository, never()).findById(any());
    }

    @Test
    void testFindByIdAndUser_Success() {
        // Given
        ProjectUserEntity projectUser = new ProjectUserEntity();
        projectUser.setProject(testProject);
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectUserRepository.findByUserIdAndProjectId(userId, projectId)).thenReturn(Optional.of(projectUser));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // When
        Optional<ProjectEntity> result = projectService.findByIdAndUser(projectId, authentication);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testProject.getId(), result.get().getId());
    }

    @Test
    void testExistsByIdAndUser_UserNotFound_ReturnsFalse() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        boolean result = projectService.existsByIdAndUser(projectId, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testExistsByIdAndUser_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectUserRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(true);

        // When
        boolean result = projectService.existsByIdAndUser(projectId, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testUpdate_Success() {
        // Given
        testProject.setStoryPointTimeMappings(new ArrayList<>());
        when(projectRepository.save(testProject)).thenReturn(testProject);

        // When
        ProjectEntity result = projectService.update(testProject);

        // Then
        assertNotNull(result);
        verify(storyPointTimeMappingRepository, times(1))
            .deleteAllForProjectNotInIds(eq(projectId), any(UUID[].class));
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void testUpdate_WithStoryPointMappings() {
        // Given
        StoryPointTimeMappingEntity mapping1 = new StoryPointTimeMappingEntity();
        mapping1.setId(UUID.randomUUID());
        StoryPointTimeMappingEntity mapping2 = new StoryPointTimeMappingEntity();
        mapping2.setId(UUID.randomUUID());
        testProject.setStoryPointTimeMappings(List.of(mapping1, mapping2));
        when(projectRepository.save(testProject)).thenReturn(testProject);

        // When
        ProjectEntity result = projectService.update(testProject);

        // Then
        assertNotNull(result);
        verify(storyPointTimeMappingRepository, times(1))
            .deleteAllForProjectNotInIds(eq(projectId), any(UUID[].class));
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void testUpdate_WithNullIdsInMappings() {
        // Given
        StoryPointTimeMappingEntity mapping1 = new StoryPointTimeMappingEntity();
        mapping1.setId(null);
        StoryPointTimeMappingEntity mapping2 = new StoryPointTimeMappingEntity();
        mapping2.setId(UUID.randomUUID());
        testProject.setStoryPointTimeMappings(List.of(mapping1, mapping2));
        when(projectRepository.save(testProject)).thenReturn(testProject);

        // When
        ProjectEntity result = projectService.update(testProject);

        // Then
        assertNotNull(result);
        verify(storyPointTimeMappingRepository, times(1))
            .deleteAllForProjectNotInIds(eq(projectId), any(UUID[].class));
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void testDelete_Success() {
        // Given
        doNothing().when(reportRepository).deleteByProjectId(projectId);
        doNothing().when(projectUserRepository).deleteByProjectId(projectId);
        doNothing().when(projectRepository).deleteById(projectId);

        // When
        projectService.delete(projectId);

        // Then
        verify(reportRepository, times(1)).deleteByProjectId(projectId);
        verify(projectUserRepository, times(1)).deleteByProjectId(projectId);
        verify(projectRepository, times(1)).deleteById(projectId);
    }
}



