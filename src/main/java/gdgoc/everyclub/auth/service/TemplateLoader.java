package gdgoc.everyclub.auth.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Service for loading email templates from the classpath.
 */
@Service
public class TemplateLoader {

    private static final String TEMPLATES_PATH = "templates/email/";

    /**
     * Loads an email template from the classpath.
     *
     * @param templateName the name of the template file (without path)
     * @return the template content as a string
     * @throws IOException if the template cannot be loaded
     */
    public String loadTemplate(String templateName) throws IOException {
        String fullPath = TEMPLATES_PATH + templateName;
        Resource resource = new ClassPathResource(fullPath);

        if (!resource.exists()) {
            throw new IOException("Template not found: " + fullPath);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Replaces placeholders in the template with actual values.
     *
     * @param template the template string
     * @param placeholders the map of placeholder names to values
     * @return the template with placeholders replaced
     */
    public String replacePlaceholders(String template, java.util.Map<String, String> placeholders) {
        String result = template;
        for (java.util.Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}