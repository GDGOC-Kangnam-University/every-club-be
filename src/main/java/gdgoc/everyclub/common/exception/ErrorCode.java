package gdgoc.everyclub.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드 인터페이스
 * 각 도메인별 ErrorCode enum이 이 인터페이스를 구현
 */
public interface ErrorCode {

    /**
     * HTTP 상태 코드
     */
    HttpStatus getStatus();

    /**
     * 에러 타입 (VALIDATION, AUTH, ACCESS, etc.)
     */
    String getType();

    /**
     * 기본 에러 메시지
     */
    String getDefaultMessage();

    /**
     * 클라이언트/문서/프론트 분기용 안정 식별자
     * 기본 구현: enum의 name() 반환
     */
    default String code() {
        if (this instanceof Enum<?>) {
            return ((Enum<?>) this).name();
        }
        return this.getClass().getSimpleName();
    }
}