package gdgoc.everyclub.common;

import gdgoc.everyclub.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(name = "ApiResponse", description = "모든 API가 사용하는 공통 응답 래퍼")
public class ApiResponse<T> {
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_ERROR = "ERROR";

    /**
     * "SUCCESS" | "ERROR"
     */
    @Schema(description = "응답 상태", example = "SUCCESS", allowableValues = {"SUCCESS", "ERROR"})
    private final String status;
    /**
     * status extends "ERROR" ? "대충 에러메세지" : null
     */
    @Schema(description = "요청 실패 시 오류 메시지", nullable = true, example = "입력값이 올바르지 않습니다.")
    private final String message;
    /**
     * status extends "SUCCESS" ? "데이터" : null
     */
    @Schema(description = "응답 데이터. 실패 시 null", nullable = true)
    private final T data;

    /**
     * @param data Nullable data to send back
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(STATUS_SUCCESS, null, data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(STATUS_SUCCESS, null, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(
                STATUS_ERROR,
                errorCode.getDefaultMessage(),
                null
        );
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(
                STATUS_ERROR,
                message,
                null
        );
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(
                STATUS_ERROR,
                message,
                null
        );
    }
}
