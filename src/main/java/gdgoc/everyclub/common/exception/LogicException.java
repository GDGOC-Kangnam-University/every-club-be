package gdgoc.everyclub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * ErrorCode를 담아주는 Exception
 */
@Getter
@RequiredArgsConstructor
public class LogicException extends RuntimeException {
    private final ErrorCode errorCode;
}
