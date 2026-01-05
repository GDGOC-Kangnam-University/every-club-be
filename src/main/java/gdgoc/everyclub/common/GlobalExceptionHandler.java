package gdgoc.everyclub.common;

import gdgoc.everyclub.common.exception.ErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static gdgoc.everyclub.common.exception.AccessErrorCode.ACCESS_DENIED;
import static gdgoc.everyclub.common.exception.SystemErrorCode.INTERNAL_ERROR;
import static gdgoc.everyclub.common.exception.ValidationErrorCode.INVALID_INPUT;

//전역 예외 처리 핸들러
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 비즈니스 로직 예외 처리 (LogicException)
    @ExceptionHandler(LogicException.class)
    public ResponseEntity<Object> handleLogicException(
            LogicException e,
            HttpServletRequest request) {

        log.warn("LogicException: [{}] {} at {}",
                e.getErrorCode().code(),
                e.getMessage(),
                request.getRequestURI());

        return handleExceptionInternal(e.getErrorCode());
    }

    // @Valid 검증 실패 예외 처리
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Validation failed: {}", ex.getBindingResult().getTarget());
        return handleExceptionInternal(ex, INVALID_INPUT);
    }

    // 접근 권한 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            HttpServletRequest request,
            AccessDeniedException e) {

        log.warn("Access denied: {} at {}", e.getMessage(), request.getRequestURI());
        return handleExceptionInternal(ACCESS_DENIED);
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(
            Exception e,
            HttpServletRequest request,
            WebRequest webRequest) {

        log.error("Unexpected exception at {}: {}", request.getRequestURI(), e.getMessage(), e);
        return handleExceptionInternal(INTERNAL_ERROR);
    }

    private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.getDefaultMessage()));
    }

    private ResponseEntity<Object> handleExceptionInternal(MethodArgumentNotValidException e, ErrorCode errorCode) {
        List<String> validationErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.getDefaultMessage() + " " + validationErrors));
    }
}