package gdgoc.everyclub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * RESOURCE - 존재/상태 문제 (404)
 */
@Getter
@RequiredArgsConstructor
public enum ResourceErrorCode implements ErrorCode {

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE",
            "요청한 리소스를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String type;
    private final String defaultMessage;
}