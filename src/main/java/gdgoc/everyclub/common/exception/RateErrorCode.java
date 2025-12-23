package gdgoc.everyclub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * RATE - 제한 초과 (429)
 */
@Getter
@RequiredArgsConstructor
public enum RateErrorCode implements ErrorCode {

    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "RATE",
            "요청 횟수 제한을 초과했습니다.");

    private final HttpStatus status;
    private final String type;
    private final String defaultMessage;
}