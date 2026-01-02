package gdgoc.everyclub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * AUTH - 인증 필요 (401)
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH",
            "인증이 필요합니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH",
            "유효하지 않은 토큰입니다."),

    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH",
            "토큰이 만료되었습니다.");

    private final HttpStatus status;
    private final String type;
    private final String defaultMessage;
}