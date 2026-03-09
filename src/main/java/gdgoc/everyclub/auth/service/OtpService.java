package gdgoc.everyclub.auth.service;

import gdgoc.everyclub.auth.config.OtpProperties;
import gdgoc.everyclub.auth.domain.EmailVerification;
import gdgoc.everyclub.auth.repository.EmailVerificationRepository;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpProperties otpProperties;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final OtpEmailService otpEmailService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates an OTP using the configured character set and length.
     */
    public String generateOtp() {
        StringBuilder otp = new StringBuilder(otpProperties.getCodeLength());
        String characterSet = otpProperties.getCharacterSet();

        for (int i = 0; i < otpProperties.getCodeLength(); i++) {
            int index = secureRandom.nextInt(characterSet.length());
            otp.append(characterSet.charAt(index));
        }

        return otp.toString();
    }

    /**
     * Saves the verification code for a user.
     * If a verification already exists for the user, it will be overwritten.
     */
    @Transactional
    public void saveVerification(Long userId, String otpCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Delete any existing verification for this user
        emailVerificationRepository.findByUser(user)
                .ifPresent(emailVerificationRepository::delete);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(otpProperties.getExpirationMinutes());

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .verificationCode(otpCode)
                .expiresAt(expiresAt)
                .build();

        emailVerificationRepository.save(verification);
    }

    /**
     * Sends OTP to the user's email.
     */
    public void sendOtpEmail(String email, String otpCode) {
        otpEmailService.sendOtpEmail(email, otpCode);
    }

    /**
     * Verifies the OTP code for a user.
     * Returns true if the code is valid and not expired.
     * Returns false if the code is invalid or expired.
     */
    @Transactional
    public boolean verifyOtp(Long userId, String code) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return false;
        }

        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findByUser(userOpt.get());

        if (verificationOpt.isEmpty()) {
            return false;
        }

        EmailVerification verification = verificationOpt.get();

        // Check if OTP is expired
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Check if OTP matches
        if (!verification.getVerificationCode().equals(code)) {
            return false;
        }

        // Verification successful - delete the verification record
        emailVerificationRepository.delete(verification);

        return true;
    }
}