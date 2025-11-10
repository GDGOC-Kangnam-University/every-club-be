package gdgoc.everyclub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 행동 가능성(Actionability) 기준 에러 코드
 * - VALIDATION: 요청 수정으로 해결 (400)
 * - AUTH: 로그인/토큰 갱신 필요 (401)
 * - ACCESS: 권한/정책 문제 (403)
 * - RESOURCE: 존재/상태 문제 (404)
 * - BUSINESS: 도메인 규칙 위반 (409)
 * - RATE: 제한 초과 (429)
 * - SYSTEM: 서버 내부 오류 (500)
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // VALIDATION - 요청 수정으로 해결 (400)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "VALIDATION",
            "입력값이 올바르지 않습니다."),

    INVALID_EMAIL_DOMAIN(HttpStatus.BAD_REQUEST, "VALIDATION",
            "허용되지 않은 이메일 도메인입니다."),

    INVALID_URL_FORMAT(HttpStatus.BAD_REQUEST, "VALIDATION",
            "URL 형식이 올바르지 않습니다."),


    // AUTH - 인증 필요 (401)
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH",
            "인증이 필요합니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH",
            "유효하지 않은 토큰입니다."),

    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH",
            "토큰이 만료되었습니다."),


    // ACCESS - 권한/정책 문제 (403)
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS",
            "접근 권한이 없습니다."),

    INSUFFICIENT_ROLE(HttpStatus.FORBIDDEN, "ACCESS",
            "필요한 권한이 부족합니다."),

    PRIVATE_RESOURCE(HttpStatus.FORBIDDEN, "ACCESS",
            "비공개 리소스입니다."),


    // RESOURCE - 존재/상태 문제 (404)
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE",
            "요청한 리소스를 찾을 수 없습니다."),


    // BUSINESS - 도메인 규칙 위반 (409)
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "BUSINESS",
            "이미 존재하는 리소스입니다."),

    ALREADY_PROCESSED(HttpStatus.CONFLICT, "BUSINESS",
            "이미 처리된 요청입니다."),

    STATE_CONFLICT(HttpStatus.CONFLICT, "BUSINESS",
            "현재 상태에서 수행할 수 없는 작업입니다."),


    // RATE - 제한 초과 (429)
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "RATE",
            "요청 횟수 제한을 초과했습니다."),

    // SYSTEM - 서버 내부 오류 (500)
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM",
            "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String type;
    private final String defaultMessage;

    /** 클라이언트/문서/프론트 분기용 안정 식별자 */
    public String code() {
        return name(); // e.g. "INVALID_INPUT"
    }
}