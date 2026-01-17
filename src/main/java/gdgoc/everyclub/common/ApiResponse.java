package gdgoc.everyclub.common;

import gdgoc.everyclub.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_ERROR = "ERROR";

    /**
     * "SUCCESS" | "ERROR"
     */
    private final String status;
    /**
     * status extends "ERROR" ? "대충 에러메세지" : null
     */
    private final String message;
    /**
     * status extends "SUCCESS" ? "데이터" : null
     */
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
