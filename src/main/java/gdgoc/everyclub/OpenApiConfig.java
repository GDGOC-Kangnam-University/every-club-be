package gdgoc.everyclub;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        var info = new Info()
                .title("EveryClub API");
        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}
