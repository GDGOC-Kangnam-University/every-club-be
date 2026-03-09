package gdgoc.everyclub.auth.service;

import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.SystemErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Placeholder implementation of OtpEmailService that throws NotImplementedException.
 * This is used until the actual SMTP email implementation is added in a separate track.
 */
@Service
public class NotImplementedOtpEmailService implements OtpEmailService {

    private static final Logger log = LoggerFactory.getLogger(NotImplementedOtpEmailService.class);

    @Override
    public void sendOtpEmail(String to, String otpCode) {
        // Log the OTP for development visibility (as per spec)
        log.debug("OTP email would be sent to: {} with code: {}", to, otpCode);
        log.info("[DEV] OTP for {}: {}", to, otpCode);

        throw new LogicException(SystemErrorCode.NOT_IMPLEMENTED);
    }
}