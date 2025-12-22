package gdgoc.everyclub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * ACCESS - 권한/정책 문제 (403)
 */
@Getter
@RequiredArgsConstructor
public enum AccessErrorCode implements ErrorCode {

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS",
            "접근 권한이 없습니다."),

    INSUFFICIENT_ROLE(HttpStatus.FORBIDDEN, "ACCESS",
            "필요한 권한이 부족합니다."),

    PRIVATE_RESOURCE(HttpStatus.FORBIDDEN, "ACCESS",
            "비공개 리소스입니다.");

    private final HttpStatus status;
    private final String type;
    private final String defaultMessage;
}