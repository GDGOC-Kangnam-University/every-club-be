package gdgoc.everyclub.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorCodeTest {

    @Test
    @DisplayName("Enum 구현체의 code() 메서드 테스트")
    void enumCodeTest() {
        // Given
        ErrorCode errorCode = AuthErrorCode.AUTHENTICATION_REQUIRED;

        // When
        String code = errorCode.code();

        // Then
        assertEquals("AUTHENTICATION_REQUIRED", code);
    }

    @Test
    @DisplayName("익명 클래스 구현체의 code() 메서드 테스트")
    void anonymousClassCodeTest() {
        // Given
        ErrorCode errorCode = new ErrorCode() {
            @Override
            public HttpStatus getStatus() {
                return HttpStatus.BAD_REQUEST;
            }

            @Override
            public String getType() {
                return "TEST";
            }

            @Override
            public String getDefaultMessage() {
                return "Test message";
            }
        };

        // When
        String code = errorCode.code();

        // Then
        // 익명 클래스의 경우 getClass().getSimpleName()은 빈 문자열을 반환할 수 있음
        // 하지만 default 메서드 로직상 Enum이 아니면 simpleName을 반환하므로
        // 여기서는 빈 문자열("")이 반환됨을 확인
        assertEquals("", code);
    }
    
    @Test
    @DisplayName("일반 클래스 구현체의 code() 메서드 테스트")
    void classCodeTest() {
        // Given
        class TestErrorCode implements ErrorCode {
            @Override
            public HttpStatus getStatus() { return HttpStatus.OK; }
            @Override
            public String getType() { return "TEST"; }
            @Override
            public String getDefaultMessage() { return "msg"; }
        }
        ErrorCode errorCode = new TestErrorCode();

        // When
        String code = errorCode.code();

        // Then
        assertEquals("TestErrorCode", code);
    }
}
