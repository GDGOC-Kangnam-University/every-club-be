package gdgoc.everyclub.auth.service;

import gdgoc.everyclub.auth.config.EmailProperties;
import gdgoc.everyclub.auth.config.OtpProperties;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SmtpOtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateLoader templateLoader;

    @Mock
    private MimeMessage mimeMessage;

    private EmailProperties emailProperties;
    private OtpProperties otpProperties;
    private SmtpOtpEmailService service;

    @BeforeEach
    void setUp() {
        emailProperties = new EmailProperties();
        emailProperties.setFrom("noreply@everyclub.com");

        otpProperties = new OtpProperties();
        otpProperties.setCodeLength(6);
        otpProperties.setExpirationMinutes(5);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        service = new SmtpOtpEmailService(mailSender, templateLoader, emailProperties, otpProperties);
    }

    @Test
    @DisplayName("SmtpOtpEmailService sends email with correct content")
    void sendsEmailWithCorrectContent() throws Exception {
        // given
        String template = "Your code is {otp}. Expires in {expiry} minutes.";
        when(templateLoader.loadTemplate("otp-email.txt")).thenReturn(template);

        // when
        service.sendOtpEmail("test@example.com", "123456");

        // then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("SmtpOtpEmailService throws OtpEmailException on template load failure")
    void throwsOnTemplateLoadFailure() throws IOException {
        // given
        when(templateLoader.loadTemplate("otp-email.txt")).thenThrow(new IOException("Template not found"));

        // when/then
        assertThatThrownBy(() -> service.sendOtpEmail("test@example.com", "123456"))
                .isInstanceOf(OtpEmailException.class)
                .hasMessageContaining("Failed to send OTP email");
    }

    @Test
    @DisplayName("SmtpOtpEmailService throws OtpEmailException on mail sender failure")
    void throwsOnMailSenderFailure() throws Exception {
        // given
        String template = "Your code is {otp}";
        when(templateLoader.loadTemplate("otp-email.txt")).thenReturn(template);
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(mimeMessage);

        // when/then
        assertThatThrownBy(() -> service.sendOtpEmail("test@example.com", "123456"))
                .isInstanceOf(OtpEmailException.class)
                .hasMessageContaining("Failed to send OTP email");
    }

    @Test
    @DisplayName("SmtpOtpEmailService maskEmail returns te***@example.com for test@example.com")
    void maskEmailNormal() {
        // when
        String masked = service.maskEmail("test@example.com");

        // then
        assertThat(masked).isEqualTo("te***@example.com");
    }

    @Test
    @DisplayName("SmtpOtpEmailService maskEmail returns ***@c.de for ab@c.de")
    void maskEmailShort() {
        // when
        String masked = service.maskEmail("ab@c.de");

        // then
        assertThat(masked).isEqualTo("***@c.de");
    }

    @Test
    @DisplayName("SmtpOtpEmailService maskEmail returns *** for null")
    void maskEmailNull() {
        // when
        String masked = service.maskEmail(null);

        // then
        assertThat(masked).isEqualTo("***");
    }

    @Test
    @DisplayName("SmtpOtpEmailService maskEmail returns *** for emails shorter than 4 chars")
    void maskEmailVeryShort() {
        // when
        String masked = service.maskEmail("a@b");

        // then
        assertThat(masked).isEqualTo("***");
    }
}