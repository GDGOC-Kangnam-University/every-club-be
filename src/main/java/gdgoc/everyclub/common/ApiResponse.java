package gdgoc.everyclub.common;

import gdgoc.everyclub.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    /**
     * "SUCCESS" | "ERROR"
     */
    private String status;
    /**
     * status extends "ERROR" ? "대충 에러메세지" : null
     */
    private String message;
    /**
     * status extends "SUCCESS" ? "데이터" : null
     */
    private T data;

    /**
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", null, data);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(
                "ERROR",
                errorCode.getDefaultMessage(),
                null
        );
    }
}

