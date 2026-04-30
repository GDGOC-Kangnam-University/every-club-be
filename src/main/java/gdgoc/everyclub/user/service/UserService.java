package gdgoc.everyclub.user.service;

import gdgoc.everyclub.auth.service.SignupService;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final SignupService signupService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long createUser(UserCreateRequest request) {
        if (!request.password().equals(request.passwordConfirm())) {
            throw new LogicException(ValidationErrorCode.PASSWORD_MISMATCH);
        }

        String email = signupService.consumeSignupToken(request.signupToken());

        if (userRepository.existsByEmail(email)) {
            throw new LogicException(BusinessErrorCode.DUPLICATE_RESOURCE);
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .studentId(request.studentId())
                .build();
        user.markEmailAsVerified();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new LogicException(BusinessErrorCode.DUPLICATE_RESOURCE);
        }
        return user.getId();
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        if (request == null) {
            throw new NullPointerException("UserUpdateRequest cannot be null");
        }
        User user = getUserById(id);
        user.updateProfile(request.nickname(), request.department(),
                request.studentId(), request.phoneNumber(), request.bio());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}
