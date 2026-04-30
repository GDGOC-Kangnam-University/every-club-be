package gdgoc.everyclub.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController implements HealthApiSpec {

    @Override
    public ApiResponse<Void> health() {
        return ApiResponse.success();
    }
}
