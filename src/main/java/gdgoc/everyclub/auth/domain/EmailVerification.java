package gdgoc.everyclub.auth.domain;

import gdgoc.everyclub.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "email_verification", uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "verification_code", nullable = false)
    private String verificationCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public EmailVerification(User user, String verificationCode, LocalDateTime expiresAt) {
        this.user = user;
        this.verificationCode = verificationCode;
        this.expiresAt = expiresAt;
    }
}