package gdgoc.everyclub.student;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

/**
 * Check if the email domain is school-issued or not.
 */
public class StudentVerificationClient {

    private final String githubToken;
    private final RestTemplate restTemplate;

    public StudentVerificationClient(String githubToken) {
        this.githubToken = githubToken;
        this.restTemplate = new RestTemplate();
    }

    /**
     * @param domain the URL of the domain to check. For example, asdf@asdf.ac.kr's domain is 'asdf.ac.kr'.
     * @see <a href="https://github.com/JetBrains/swot">Repository for domain data</a>
     * @see <a href="https://docs.github.com/ko/rest/repos/contents?apiVersion=2022-11-28#get-repository-content">Github doc</a>
     */
    public boolean check(String domain) {
        String[] pathSegments = domain.split("\\.");
        Collections.reverse(Arrays.asList(pathSegments));
        String path = String.join("/", pathSegments);
        String endpoint = "https://api.github.com/repos/JetBrains/swot/contents/lib/domains/" + path + ".txt";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.raw+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");

        if (githubToken != null && !githubToken.isEmpty()) {
            headers.setBearerAuth(githubToken);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
