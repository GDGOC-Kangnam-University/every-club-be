package gdgoc.everyclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EveryClubApplication {

    public static void main(String[] args) {
        SpringApplication.run(EveryClubApplication.class, args);
    }

}