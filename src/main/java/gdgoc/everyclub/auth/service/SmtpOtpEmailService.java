package gdgoc.everyclub.auth.service;

import gdgoc.everyclub.auth.config.EmailProperties;
import gdgoc.everyclub.auth.config.OtpProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * SMTP-based implementation of OtpEmailService for sending OTP verification emails.
 */
@Component
@Primary
public class SmtpOtpEmailService implements OtpEmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpOtpEmailService.class);
    private static final String OTP_TEMPLATE = "otp-email.txt";

    private final JavaMailSender mailSender;
    private final TemplateLoader templateLoader;
    private final EmailProperties emailProperties;
    private final OtpProperties otpProperties;

    public SmtpOtpEmailService(
            JavaMailSender mailSender,
            TemplateLoader templateLoader,
            EmailProperties emailProperties,
            OtpProperties otpProperties) {
        this.mailSender = mailSender;
        this.templateLoader = templateLoader;
        this.emailProperties = emailProperties;
        this.otpProperties = otpProperties;
    }

    @Override
    @Async
    public void sendOtpEmail(String to, String otpCode) {
        try {
            String content = buildEmailContent(otpCode);
            sendEmail(to, "Your Verification Code", content);
            log.info("OTP email sent successfully to: {}", maskEmail(to));
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", maskEmail(to), e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildEmailContent(String otpCode) throws IOException {
        String template = templateLoader.loadTemplate(OTP_TEMPLATE);
        return templateLoader.replacePlaceholders(template, Map.of(
                "otp", otpCode,
                "expiry", String.valueOf(otpProperties.getExpirationMinutes())
        ));
    }

    private void sendEmail(String to, String subject, String content) throws MailException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(emailProperties.getFrom());
        message.setRecipients(MimeMessage.RecipientType.TO, to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    private String maskEmail(String email) {
        if (email == null || email.length() < 4) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}