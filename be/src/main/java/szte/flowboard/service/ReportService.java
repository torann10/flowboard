package szte.flowboard.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import szte.flowboard.entity.ReportEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.ReportRepository;
import szte.flowboard.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    public ReportEntity create(ReportEntity report, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        report.setUserId(user.get().getId());
        
        return reportRepository.save(report);
    }

    public List<ReportEntity> findAllByUser(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return List.of();
        }
        
        return reportRepository.findByUserId(user.get().getId());
    }

    public Optional<ReportEntity> findByIdAndUser(UUID id, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return Optional.empty();
        }
        
        return reportRepository.findByIdAndUserId(id, user.get().getId());
    }

    public ReportEntity update(ReportEntity report, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        report.setUserId(user.get().getId());
        
        return reportRepository.save(report);
    }

    public void delete(UUID id) {
        reportRepository.deleteById(id);
    }

    public boolean existsByIdAndUser(UUID id, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return false;
        }
        
        return reportRepository.existsByIdAndUserId(id, user.get().getId());
    }

    public long countByUser(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return 0;
        }
        
        return reportRepository.countByUserId(user.get().getId());
    }

    private String getKeycloakIdFromAuthentication(Authentication authentication) {
        return (String) ((Jwt) authentication.getPrincipal()).getClaims().get("sub");
    }
}
