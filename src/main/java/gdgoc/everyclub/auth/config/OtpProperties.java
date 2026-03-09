package gdgoc.everyclub.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {
    private int codeLength = 64;
    private int expirationMinutes = 2;
    private String characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
}