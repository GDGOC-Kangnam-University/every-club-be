package gdgoc.everyclub.storage.config;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class S3ConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void amazonS3BeanShouldBeCreated() {
        AmazonS3 amazonS3 = applicationContext.getBean(AmazonS3.class);
        assertThat(amazonS3).isNotNull();
    }
}
