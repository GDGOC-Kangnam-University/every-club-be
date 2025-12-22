package gdgoc.everyclub.common;

import gdgoc.everyclub.common.exception.ErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LogicException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(LogicException e) {

        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }
}