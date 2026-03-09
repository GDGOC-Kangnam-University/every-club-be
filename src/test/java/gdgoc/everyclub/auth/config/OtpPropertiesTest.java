package gdgoc.everyclub.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OtpPropertiesTest {

    @Autowired
    private OtpProperties otpProperties;

    @Test
    void shouldHaveDefaultCodeLength() {
        assertThat(otpProperties.getCodeLength()).isEqualTo(64);
    }

    @Test
    void shouldHaveDefaultExpirationMinutes() {
        assertThat(otpProperties.getExpirationMinutes()).isEqualTo(2);
    }

    @Test
    void shouldHaveDefaultCharacterSet() {
        assertThat(otpProperties.getCharacterSet()).isEqualTo("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
    }

    @Test
    void shouldBeAbleToBindCustomProperties() {
        // Test that the properties are correctly bound from configuration
        assertThat(otpProperties).isNotNull();
        assertThat(otpProperties.getCodeLength()).isPositive();
        assertThat(otpProperties.getExpirationMinutes()).isPositive();
        assertThat(otpProperties.getCharacterSet()).isNotEmpty();
    }
}