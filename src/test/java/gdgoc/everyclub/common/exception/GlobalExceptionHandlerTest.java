package gdgoc.everyclub.common.exception;

import gdgoc.everyclub.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test/uri");
    }

    @Test
    @DisplayName("ApiException 처리 테스트")
    void handleApiException() {
        // Given
        ErrorCode errorCode = AuthErrorCode.AUTHENTICATION_REQUIRED;
        ApiException apiException = new ApiException(errorCode);

        // When
        ResponseEntity<ApiResponse<?>> response = globalExceptionHandler.handleApiException(request, apiException);

        // Then
        assertEquals(errorCode.getStatus(), response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorCode.getDefaultMessage(), response.getBody().getMessage());
    }

    @Test
    @DisplayName("LogicException 처리 테스트")
    void handleLogicException() {
        // Given
        ErrorCode errorCode = AuthErrorCode.INVALID_TOKEN;
        LogicException logicException = new LogicException(errorCode);

        // When
        ResponseEntity<ApiResponse<?>> response = globalExceptionHandler.handleLogicException(request, logicException);

        // Then
        assertEquals(errorCode.getStatus(), response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorCode.getDefaultMessage(), response.getBody().getMessage());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 처리 테스트")
    void handleValidationException() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        ObjectError objectError = new ObjectError("testObject", "Validation failed message");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(objectError));

        // When
        ResponseEntity<ApiResponse<?>> response = globalExceptionHandler.handleValidationException(request, exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed message", response.getBody().getMessage());
    }

    @Test
    @DisplayName("AccessDeniedException 처리 테스트")
    void handleAccessDeniedException() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ApiResponse<?>> response = globalExceptionHandler.handleAccessDeniedException(request, exception);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AccessErrorCode.ACCESS_DENIED.getDefaultMessage(), response.getBody().getMessage());
    }

    @Test
    @DisplayName("RuntimeException 처리 테스트")
    void handleRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Runtime error");

        // When
        ResponseEntity<ApiResponse<?>> response = globalExceptionHandler.handleRuntimeException(request, exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SystemErrorCode.INTERNAL_ERROR.getDefaultMessage(), response.getBody().getMessage());
    }

    @Test
    @DisplayName("Exception 처리 테스트")
    void handleException() {
        // Given
        Exception exception = new Exception("Unexpected error");

        // When
        ResponseEntity<ApiResponse<?>> response = globalExceptionHandler.handleException(request, exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SystemErrorCode.INTERNAL_ERROR.getDefaultMessage(), response.getBody().getMessage());
    }
}
