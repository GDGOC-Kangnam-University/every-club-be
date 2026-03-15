package gdgoc.everyclub.auth.service;

/**
 * Custom runtime exception for OTP email failures.
 */
public class OtpEmailException extends RuntimeException {

    public OtpEmailException(String message) {
        super(message);
    }

    public OtpEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}