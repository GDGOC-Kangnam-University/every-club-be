package gdgoc.everyclub.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Health", description = "서버 상태 확인")
public interface HealthApiSpec {

    @GetMapping
    @Operation(summary = "헬스체크", description = "서버가 정상적으로 실행 중인지 확인합니다.")
    ApiResponse<Void> health();
}
