package gdgoc.everyclub.auth.service;

/**
 * Service interface for sending OTP emails.
 * This interface is designed for future SMTP implementation in a separate track.
 */
public interface OtpEmailService {

    /**
     * Sends an OTP code to the specified email address.
     *
     * @param to      the recipient email address
     * @param otpCode the OTP code to send
     */
    void sendOtpEmail(String to, String otpCode);
}