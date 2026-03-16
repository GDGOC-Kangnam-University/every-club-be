package gdgoc.everyclub.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateLoaderTest {

    private TemplateLoader templateLoader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        templateLoader = new TemplateLoader();
    }

    @Test
    @DisplayName("TemplateLoader can replace placeholders in template")
    void canReplacePlaceholders() {
        // given
        String template = "Your code is {otp}, expires in {expiry} minutes";

        // when
        String result = templateLoader.replacePlaceholders(template, Map.of("otp", "123456", "expiry", "5"));

        // then
        assertThat(result).isEqualTo("Your code is 123456, expires in 5 minutes");
    }

    @Test
    @DisplayName("TemplateLoader handles missing placeholders gracefully")
    void handlesMissingPlaceholders() {
        // given
        String template = "Your code is {otp}";

        // when
        String result = templateLoader.replacePlaceholders(template, Map.of("other", "value"));

        // then
        assertThat(result).isEqualTo("Your code is {otp}");
    }

    @Test
    @DisplayName("TemplateLoader handles empty placeholder map")
    void handlesEmptyPlaceholderMap() {
        // given
        String template = "Your code is {otp}";

        // when
        String result = templateLoader.replacePlaceholders(template, Map.of());

        // then
        assertThat(result).isEqualTo("Your code is {otp}");
    }

    @Test
    @DisplayName("TemplateLoader loads otp-email.txt template from classpath")
    void loadsOtpEmailTemplate() throws IOException {
        // when
        String template = templateLoader.loadTemplate("otp-email.txt");

        // then
        assertThat(template).contains("{otp}");
        assertThat(template).contains("{expiry}");
    }

    @Test
    @DisplayName("TemplateLoader throws IOException for non-existent template")
    void throwsForNonExistentTemplate() {
        // when/then
        assertThatThrownBy(() -> templateLoader.loadTemplate("nonexistent.txt"))
                .isInstanceOf(IOException.class);
    }
}