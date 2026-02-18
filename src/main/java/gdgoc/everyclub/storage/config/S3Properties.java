package gdgoc.everyclub.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "s3")
public class S3Properties {
    private String endpoint;
    private String region;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private int expirationMinutes = 5;
}
