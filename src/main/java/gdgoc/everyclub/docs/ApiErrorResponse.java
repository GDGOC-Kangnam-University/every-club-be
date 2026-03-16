package gdgoc.everyclub.docs;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiErrorResponse", description = "공통 오류 응답 래퍼")
public record ApiErrorResponse(
        @Schema(description = "응답 상태", example = "ERROR", allowableValues = {"SUCCESS", "ERROR"})
        String status,

        @Schema(description = "사용자에게 보여줄 오류 메시지", example = "입력값이 올바르지 않습니다.")
        String message,

        @Schema(description = "오류 응답에서는 항상 null", nullable = true)
        Object data
) {
}
