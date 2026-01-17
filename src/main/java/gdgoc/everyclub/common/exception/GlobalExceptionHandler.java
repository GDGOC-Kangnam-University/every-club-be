package gdgoc.everyclub.common.exception;

import gdgoc.everyclub.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.AccessDeniedException;

import static gdgoc.everyclub.common.exception.AccessErrorCode.ACCESS_DENIED;
import static gdgoc.everyclub.common.exception.SystemErrorCode.INTERNAL_ERROR;
import static gdgoc.everyclub.common.exception.ValidationErrorCode.INVALID_INPUT;


//전역 예외 처리 핸들러
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 커스텀 API 예외 처리
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<?>> handleApiException(
            HttpServletRequest request,
            ApiException e) {

        log.warn("ApiException: [{}] {} at {}",
                e.getErrorCode().code(),
                e.getMessage(),
                request.getRequestURI());

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(e.getErrorCode()));
    }

    // ErrorCode를 담는 예외 처리
    @ExceptionHandler(LogicException.class)
    public ResponseEntity<ApiResponse<?>> handleLogicException(
            HttpServletRequest request,
            LogicException e) {

        log.warn("LogicException: [{}] {} at {}",
                e.getErrorCode().code(),
                e.getErrorCode().getDefaultMessage(),
                request.getRequestURI());

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(e.getErrorCode()));
    }

    // @Valid 검증 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            HttpServletRequest request,
            MethodArgumentNotValidException e) {

        BindingResult bindingResult = e.getBindingResult();
        String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();

        log.warn("Validation failed: {} at {}", errorMessage, request.getRequestURI());

        // TODO: ApiResponse.error(ErrorCode errorCode, String customMessage) 메서드 필요
        return ResponseEntity
                .status(INVALID_INPUT.getStatus())
                .body(ApiResponse.error(INVALID_INPUT, errorMessage));
    }

    // 접근 권한 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
            HttpServletRequest request,
            AccessDeniedException e) {

        log.warn("Access denied: {} at {}", e.getMessage(), request.getRequestURI());

        return ResponseEntity
                .status(ACCESS_DENIED.getStatus())
                .body(ApiResponse.error(ACCESS_DENIED));
    }

    // 런타임 예외 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(
            HttpServletRequest request,
            RuntimeException e) {

        log.error("RuntimeException at {}: {}", request.getRequestURI(), e.getMessage(), e);

        return ResponseEntity
                .status(INTERNAL_ERROR.getStatus())
                .body(ApiResponse.error(INTERNAL_ERROR));
    }

    // 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(
            HttpServletRequest request,
            Exception e) {

        log.error("Unexpected exception at {}: {}", request.getRequestURI(), e.getMessage(), e);

        return ResponseEntity
                .status(INTERNAL_ERROR.getStatus())
                .body(ApiResponse.error(INTERNAL_ERROR));
    }
}