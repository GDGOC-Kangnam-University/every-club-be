package gdgoc.everyclub.auth.service;

import gdgoc.everyclub.auth.config.OtpProperties;
import gdgoc.everyclub.auth.domain.EmailVerification;
import gdgoc.everyclub.auth.repository.EmailVerificationRepository;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpProperties otpProperties;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpEmailService otpEmailService;

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService(
                otpProperties,
                emailVerificationRepository,
                userRepository,
                otpEmailService
        );
    }

    @Test
    @DisplayName("generateOtp generates OTP with configured length")
    void generateOtp_WithConfiguredLength() {
        // given
        when(otpProperties.getCodeLength()).thenReturn(8);
        when(otpProperties.getCharacterSet()).thenReturn("ABC123");

        // when
        String otp = otpService.generateOtp();

        // then
        assertThat(otp).hasSize(8);
        assertThat(otp).matches("[ABC123]{8}");
    }

    @Test
    @DisplayName("generateOtp uses configured character set")
    void generateOtp_UsesCharacterSet() {
        // given
        when(otpProperties.getCodeLength()).thenReturn(10);
        when(otpProperties.getCharacterSet()).thenReturn("AB");

        // when
        String otp = otpService.generateOtp();

        // then
        assertThat(otp).hasSize(10);
        assertThat(otp).matches("[AB]{10}");
    }

    @Test
    @DisplayName("saveVerification saves new verification for user")
    void saveVerification_SavesNewVerification() {
        // given
        Long userId = 1L;
        User user = User.builder().email("test@example.com").build();
        String otpCode = "ABC123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.empty());
        when(otpProperties.getExpirationMinutes()).thenReturn(2);

        // when
        otpService.saveVerification(userId, otpCode);

        // then
        verify(emailVerificationRepository).save(any(EmailVerification.class));
    }

    @Test
    @DisplayName("saveVerification overwrites existing verification")
    void saveVerification_OverwritesExisting() {
        // given
        Long userId = 1L;
        User user = User.builder().email("test@example.com").build();
        EmailVerification existingVerification = EmailVerification.builder()
                .user(user)
                .verificationCode("OLD123")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.of(existingVerification));
        when(otpProperties.getExpirationMinutes()).thenReturn(2);

        // when
        otpService.saveVerification(userId, "NEW123");

        // then
        verify(emailVerificationRepository).delete(existingVerification);
        verify(emailVerificationRepository).save(any(EmailVerification.class));
    }

    @Test
    @DisplayName("verifyOtp returns true for valid code")
    void verifyOtp_ValidCode_ReturnsTrue() {
        // given
        Long userId = 1L;
        User user = User.builder().email("test@example.com").build();
        String code = "ABC123";
        LocalDateTime futureExpiry = LocalDateTime.now().plusMinutes(5);

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .verificationCode(code)
                .expiresAt(futureExpiry)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.of(verification));

        // when
        boolean result = otpService.verifyOtp(userId, code);

        // then
        assertThat(result).isTrue();
        verify(emailVerificationRepository).delete(verification);
    }

    @Test
    @DisplayName("verifyOtp returns false for invalid code")
    void verifyOtp_InvalidCode_ReturnsFalse() {
        // given
        Long userId = 1L;
        User user = User.builder().email("test@example.com").build();
        LocalDateTime futureExpiry = LocalDateTime.now().plusMinutes(5);

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .verificationCode("ABC123")
                .expiresAt(futureExpiry)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.of(verification));

        // when
        boolean result = otpService.verifyOtp(userId, "WRONG");

        // then
        assertThat(result).isFalse();
        verify(emailVerificationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("verifyOtp returns false for expired code")
    void verifyOtp_ExpiredCode_ReturnsFalse() {
        // given
        Long userId = 1L;
        User user = User.builder().email("test@example.com").build();
        LocalDateTime pastExpiry = LocalDateTime.now().minusMinutes(1);

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .verificationCode("ABC123")
                .expiresAt(pastExpiry)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.of(verification));

        // when
        boolean result = otpService.verifyOtp(userId, "ABC123");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verifyOtp returns false when user not found")
    void verifyOtp_UserNotFound_ReturnsFalse() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        boolean result = otpService.verifyOtp(userId, "ABC123");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verifyOtp returns false when no verification exists")
    void verifyOtp_NoVerification_ReturnsFalse() {
        // given
        Long userId = 1L;
        User user = User.builder().email("test@example.com").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(Optional.empty());

        // when
        boolean result = otpService.verifyOtp(userId, "ABC123");

        // then
        assertThat(result).isFalse();
    }
}