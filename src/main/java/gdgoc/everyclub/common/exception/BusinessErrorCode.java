package gdgoc.everyclub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * BUSINESS - 도메인 규칙 위반 (409)
 */
@Getter
@RequiredArgsConstructor
public enum BusinessErrorCode implements ErrorCode {

    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "BUSINESS",
            "이미 존재하는 리소스입니다."),

    ALREADY_PROCESSED(HttpStatus.CONFLICT, "BUSINESS",
            "이미 처리된 요청입니다."),

    STATE_CONFLICT(HttpStatus.CONFLICT, "BUSINESS",
            "현재 상태에서 수행할 수 없는 작업입니다.");

    private final HttpStatus status;
    private final String type;
    private final String defaultMessage;
}