package gdgoc.everyclub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
/**
 * ErrorCode를 담아주는 Exception
 *
 */
public class LogicException extends RuntimeException {
    private final ErrorCode errorCode;
}
