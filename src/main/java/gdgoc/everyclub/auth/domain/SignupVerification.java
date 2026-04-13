package gdgoc.everyclub.auth.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "signup_verification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(name = "otp_expires_at", nullable = false)
    private LocalDateTime otpExpiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "signup_token_hash")
    private String signupTokenHash;

    @Column(name = "signup_token_expires_at")
    private LocalDateTime signupTokenExpiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "resend_count", nullable = false)
    private int resendCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public SignupVerification(String email, String otpCode, LocalDateTime otpExpiresAt) {
        this.email = email;
        this.otpCode = otpCode;
        this.otpExpiresAt = otpExpiresAt;
    }

    public boolean isOtpValid(String code) {
        return !used && otpCode.equals(code) && LocalDateTime.now().isBefore(otpExpiresAt);
    }

    public void recordAttempt() {
        this.attemptCount++;
    }

    public void markVerified(String signupTokenHash, LocalDateTime signupTokenExpiresAt) {
        this.verifiedAt = LocalDateTime.now();
        this.signupTokenHash = signupTokenHash;
        this.signupTokenExpiresAt = signupTokenExpiresAt;
    }

    public boolean isSignupTokenValid(String tokenHash) {
        return verifiedAt != null
                && !used
                && signupTokenHash != null
                && signupTokenHash.equals(tokenHash)
                && LocalDateTime.now().isBefore(signupTokenExpiresAt);
    }

    public void markUsed() {
        this.used = true;
    }

    public void incrementResendCount() {
        this.resendCount++;
    }
}