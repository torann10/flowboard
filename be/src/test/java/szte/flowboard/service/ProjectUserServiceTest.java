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
import szte.flowboard.entity.UserEntity;
import szte.flowboard.enums.UserRole;
import szte.flowboard.repository.ProjectUserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectUserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ProjectUserRepository projectUserRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private ProjectUserService projectUserService;

    private UserEntity testUser;
    private ProjectEntity testProject;
    private ProjectUserEntity testProjectUser;
    private UUID userId;
    private UUID projectId;
    private UUID projectUserId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        projectUserId = UUID.randomUUID();

        testUser = new UserEntity();
        testUser.setId(userId);
        testUser.setEmailAddress("test@example.com");

        testProject = new ProjectEntity();
        testProject.setId(projectId);
        testProject.setName("Test Project");

        testProjectUser = new ProjectUserEntity();
        testProjectUser.setId(projectUserId);
        testProjectUser.setUser(testUser);
        testProjectUser.setProject(testProject);
        testProjectUser.setRole(UserRole.MEMBER);
        testProjectUser.setFee(100.0);
    }

    @Test
    void testCreate_UserNotFound_ReturnsNull() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.empty());

        // When
        ProjectUserEntity result = projectUserService.create(testProjectUser, authentication);

        // Then
        assertNull(result);
        verify(projectUserRepository, never()).save(any());
    }

    @Test
    void testCreate_NotMaintainer_ReturnsNull() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.MAINTAINER))
            .thenReturn(false);

        // When
        ProjectUserEntity result = projectUserService.create(testProjectUser, authentication);

        // Then
        assertNull(result);
        verify(projectUserRepository, never()).save(any());
    }

    @Test
    void testCreate_Success() {
        // Given
        when(userService.getUserByAuthentication(authentication)).thenReturn(Optional.of(testUser));
        when(projectUserRepository.existsByUserIdAndProjectIdAndRole(userId, projectId, UserRole.MAINTAINER))
            .thenReturn(true);
        when(projectUserRepository.save(testProjectUser)).thenReturn(testProjectUser);

        // When
        ProjectUserEntity result = projectUserService.create(testProjectUser, authentication);

        // Then
        assertNotNull(result);
        assertEquals(testProjectUser.getId(), result.getId());
        verify(projectUserRepository, times(1)).save(testProjectUser);
    }

    @Test
    void testFindAll_Success() {
        // Given
        when(projectUserRepository.findAll()).thenReturn(List.of(testProjectUser));

        // When
        List<ProjectUserEntity> result = projectUserService.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(testProjectUser.getId(), result.get(0).getId());
    }

    @Test
    void testFindById_Success() {
        // Given
        when(projectUserRepository.findById(projectUserId)).thenReturn(Optional.of(testProjectUser));

        // When
        Optional<ProjectUserEntity> result = projectUserService.findById(projectUserId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testProjectUser.getId(), result.get().getId());
    }

    @Test
    void testFindById_NotFound_ReturnsEmpty() {
        // Given
        when(projectUserRepository.findById(projectUserId)).thenReturn(Optional.empty());

        // When
        Optional<ProjectUserEntity> result = projectUserService.findById(projectUserId);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_NotFound_ReturnsNull() {
        // Given
        when(projectUserRepository.findById(projectUserId)).thenReturn(Optional.empty());

        // When
        ProjectUserEntity result = projectUserService.update(projectUserId, UserRole.MEMBER, 150.0);

        // Then
        assertNull(result);
        verify(projectUserRepository, never()).save(any());
    }

    @Test
    void testUpdate_NotMaintainer_ReturnsNull() {
        // Given
        testProjectUser.setRole(UserRole.MEMBER);
        when(projectUserRepository.findById(projectUserId)).thenReturn(Optional.of(testProjectUser));

        // When
        ProjectUserEntity result = projectUserService.update(projectUserId, UserRole.REPORTER, 150.0);

        // Then
        assertNull(result);
        verify(projectUserRepository, never()).save(any());
    }

    @Test
    void testUpdate_Success() {
        // Given
        testProjectUser.setRole(UserRole.MAINTAINER);
        when(projectUserRepository.findById(projectUserId)).thenReturn(Optional.of(testProjectUser));
        when(projectUserRepository.save(any(ProjectUserEntity.class))).thenAnswer(invocation -> {
            ProjectUserEntity pu = invocation.getArgument(0);
            return pu;
        });

        // When
        ProjectUserEntity result = projectUserService.update(projectUserId, UserRole.REPORTER, 200.0);

        // Then
        assertNotNull(result);
        assertEquals(UserRole.REPORTER, result.getRole());
        assertEquals(200.0, result.getFee());
        verify(projectUserRepository, times(1)).save(argThat(pu -> 
            pu.getRole() == UserRole.REPORTER && pu.getFee() == 200.0
        ));
    }

    @Test
    void testDelete_Success() {
        // Given
        doNothing().when(projectUserRepository).deleteById(projectUserId);

        // When
        projectUserService.delete(projectUserId);

        // Then
        verify(projectUserRepository, times(1)).deleteById(projectUserId);
    }

    @Test
    void testExistsById_Success() {
        // Given
        when(projectUserRepository.existsById(projectUserId)).thenReturn(true);

        // When
        boolean result = projectUserService.existsById(projectUserId);

        // Then
        assertTrue(result);
    }

    @Test
    void testExistsById_NotFound_ReturnsFalse() {
        // Given
        when(projectUserRepository.existsById(projectUserId)).thenReturn(false);

        // When
        boolean result = projectUserService.existsById(projectUserId);

        // Then
        assertFalse(result);
    }

    @Test
    void testUpdate_WithNullFee() {
        // Given
        testProjectUser.setRole(UserRole.MAINTAINER);
        when(projectUserRepository.findById(projectUserId)).thenReturn(Optional.of(testProjectUser));
        when(projectUserRepository.save(any(ProjectUserEntity.class))).thenAnswer(invocation -> {
            ProjectUserEntity pu = invocation.getArgument(0);
            return pu;
        });

        // When
        ProjectUserEntity result = projectUserService.update(projectUserId, UserRole.MEMBER, null);

        // Then
        assertNotNull(result);
        assertEquals(UserRole.MEMBER, result.getRole());
        assertNull(result.getFee());
    }

    @Test
    void testFindAll_EmptyList() {
        // Given
        when(projectUserRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<ProjectUserEntity> result = projectUserService.findAll();

        // Then
        assertTrue(result.isEmpty());
    }
}



