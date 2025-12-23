package gdgoc.everyclub.user.service;

import gdgoc.everyclub.common.exception.ErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;


    @Transactional
    public Long createUser(UserCreateRequest request) {
        User user = new User(request);
        userRepository.save(user);
        return user.getId();
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new LogicException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = getUserById(id);
        user.update(request.name());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}
