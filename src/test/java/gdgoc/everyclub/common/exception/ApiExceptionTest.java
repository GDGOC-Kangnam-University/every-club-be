package gdgoc.everyclub.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiExceptionTest {

    @Test
    @DisplayName("ErrorCode만 받는 생성자 테스트")
    void constructorWithErrorCode() {
        // Given
        ErrorCode errorCode = AuthErrorCode.AUTHENTICATION_REQUIRED;

        // When
        ApiException exception = new ApiException(errorCode);

        // Then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorCode.getDefaultMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("ErrorCode와 Custom Message를 받는 생성자 테스트")
    void constructorWithErrorCodeAndMessage() {
        // Given
        ErrorCode errorCode = AuthErrorCode.INVALID_TOKEN;
        String customMessage = "Custom error message";

        // When
        ApiException exception = new ApiException(errorCode, customMessage);

        // Then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    @DisplayName("ErrorCode와 Cause를 받는 생성자 테스트")
    void constructorWithErrorCodeAndCause() {
        // Given
        ErrorCode errorCode = AuthErrorCode.EXPIRED_TOKEN;
        Throwable cause = new RuntimeException("Original cause");

        // When
        ApiException exception = new ApiException(errorCode, cause);

        // Then
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorCode.getDefaultMessage(), exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
