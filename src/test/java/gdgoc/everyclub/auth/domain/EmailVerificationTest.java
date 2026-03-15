package gdgoc.everyclub.auth.domain;

import gdgoc.everyclub.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationTest {

    @Test
    @DisplayName("EmailVerification creation sets all fields correctly")
    void createEmailVerification_WithAllFields() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .build();
        String verificationCode = "ABC123";
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(2);

        // when
        EmailVerification emailVerification = EmailVerification.builder()
                .user(user)
                .verificationCode(verificationCode)
                .expiresAt(expiresAt)
                .build();

        // then
        assertThat(emailVerification.getUser()).isEqualTo(user);
        assertThat(emailVerification.getVerificationCode()).isEqualTo(verificationCode);
        assertThat(emailVerification.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("EmailVerification has relationship with User")
    void emailVerificationHasRelationshipWithUser() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .build();

        // when
        EmailVerification emailVerification = EmailVerification.builder()
                .user(user)
                .verificationCode("TEST123")
                .expiresAt(LocalDateTime.now().plusMinutes(2))
                .build();

        // then
        assertThat(emailVerification.getUser()).isNotNull();
        assertThat(emailVerification.getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("EmailVerification has createdAt field")
    void createdAtFieldExists() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .build();

        // when
        EmailVerification emailVerification = EmailVerification.builder()
                .user(user)
                .verificationCode("ABC123")
                .expiresAt(LocalDateTime.now().plusMinutes(2))
                .build();

        // then - createdAt will be set by @CreationTimestamp when persisted
        // In unit test without DB, the field exists but is null
        assertThat(emailVerification).hasFieldOrProperty("createdAt");
    }
}