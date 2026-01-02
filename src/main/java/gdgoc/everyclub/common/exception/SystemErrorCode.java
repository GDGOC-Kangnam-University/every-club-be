package gdgoc.everyclub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * SYSTEM - 서버 내부 오류 (500)
 */
@Getter
@RequiredArgsConstructor
public enum SystemErrorCode implements ErrorCode {

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM",
            "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String type;
    private final String defaultMessage;
}