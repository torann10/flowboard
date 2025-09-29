package szte.flowboard.service;

import org.springframework.stereotype.Service;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity create(UserEntity user) {
        return userRepository.save(user);
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> findById(UUID id) {
        return userRepository.findById(id);
    }

    public UserEntity update(UserEntity user) {
        return userRepository.save(user);
    }

    public void delete(UUID id) {
        userRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    public long count() {
        return userRepository.count();
    }
}