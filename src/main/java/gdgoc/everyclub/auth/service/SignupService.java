package gdgoc.everyclub.auth.service;

import gdgoc.everyclub.auth.EmailService;
import gdgoc.everyclub.auth.config.OtpProperties;
import gdgoc.everyclub.auth.domain.SignupVerification;
import gdgoc.everyclub.auth.dto.SignupTokenResponse;
import gdgoc.everyclub.auth.repository.SignupVerificationRepository;
import gdgoc.everyclub.common.exception.AuthErrorCode;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
import gdgoc.everyclub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignupService {

    private static final int SIGNUP_TOKEN_EXPIRY_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final SignupVerificationRepository signupVerificationRepository;
    private final UserRepository userRepository;
    private final OtpEmailService otpEmailService;
    private final OtpProperties otpProperties;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void sendOtp(String email) {
        if (!emailService.isSchoolEmail(email)) {
            throw new LogicException(ValidationErrorCode.INVALID_EMAIL_DOMAIN);
        }
        if (userRepository.existsByEmail(email)) {
            throw new LogicException(BusinessErrorCode.DUPLICATE_RESOURCE);
        }

        // 기존 미사용 검증 레코드 삭제 후 새로 생성
        signupVerificationRepository.deleteByEmail(email);

        String otpCode = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpProperties.getExpirationMinutes());

        SignupVerification verification = SignupVerification.builder()
                .email(email)
                .otpCode(otpCode)
                .otpExpiresAt(expiresAt)
                .build();
        signupVerificationRepository.save(verification);

        otpEmailService.sendOtpEmail(email, otpCode);
    }

    @Transactional
    public SignupTokenResponse verifyOtp(String email, String code) {
        SignupVerification verification = signupVerificationRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new LogicException(AuthErrorCode.INVALID_OTP));

        verification.recordAttempt();

        if (verification.getAttemptCount() > MAX_OTP_ATTEMPTS) {
            throw new LogicException(AuthErrorCode.INVALID_OTP);
        }

        if (!verification.isOtpValid(code)) {
            throw new LogicException(AuthErrorCode.INVALID_OTP);
        }

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hash(rawToken);
        LocalDateTime tokenExpiresAt = LocalDateTime.now().plusMinutes(SIGNUP_TOKEN_EXPIRY_MINUTES);

        verification.markVerified(tokenHash, tokenExpiresAt);

        return new SignupTokenResponse(rawToken, SIGNUP_TOKEN_EXPIRY_MINUTES * 60L);
    }

    @Transactional
    public String consumeSignupToken(String rawToken) {
        String tokenHash = hash(rawToken);

        SignupVerification verification = signupVerificationRepository
                .findBySignupTokenHash(tokenHash)
                .orElseThrow(() -> new LogicException(AuthErrorCode.INVALID_SIGNUP_TOKEN));

        if (!verification.isSignupTokenValid(tokenHash)) {
            throw new LogicException(AuthErrorCode.INVALID_SIGNUP_TOKEN);
        }

        verification.markUsed();
        return verification.getEmail();
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder(otpProperties.getCodeLength());
        String characterSet = otpProperties.getCharacterSet();
        for (int i = 0; i < otpProperties.getCodeLength(); i++) {
            otp.append(characterSet.charAt(secureRandom.nextInt(characterSet.length())));
        }
        return otp.toString();
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}