package gdgoc.everyclub.user.service;

import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UUID createUser(UserCreateRequest request) {
        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getStudentNumber()
        );
        userRepository.save(user);
        return user.getPublicId();
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserByPublicId(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with publicId: " + publicId));
    }

    @Transactional
    public void updateUser(UUID publicId, UserUpdateRequest request) {
        User user = getUserByPublicId(publicId);
        user.update(request.getName(), request.getStudentNumber());
    }

    @Transactional
    public void deleteUser(UUID publicId) {
        User user = getUserByPublicId(publicId);
        userRepository.delete(user);
    }
}
