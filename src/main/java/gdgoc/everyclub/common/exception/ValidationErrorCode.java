package gdgoc.everyclub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * VALIDATION - 요청 수정으로 해결 (400)
 */
@Getter
@RequiredArgsConstructor
public enum ValidationErrorCode implements ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "VALIDATION",
            "입력값이 올바르지 않습니다."),

    INVALID_EMAIL_DOMAIN(HttpStatus.BAD_REQUEST, "VALIDATION",
            "허용되지 않은 이메일 도메인입니다."),

    INVALID_URL_FORMAT(HttpStatus.BAD_REQUEST, "VALIDATION",
            "URL 형식이 올바르지 않습니다."),

    MAJOR_REQUIRED_FOR_DEPARTMENT_CLUB(HttpStatus.BAD_REQUEST, "VALIDATION",
            "학과동아리의 경우 소속 학과(majorId)는 필수입니다."),

    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "VALIDATION",
            "비밀번호와 비밀번호 확인이 일치하지 않습니다.");

    private final HttpStatus status;
    private final String type;
    private final String defaultMessage;
}