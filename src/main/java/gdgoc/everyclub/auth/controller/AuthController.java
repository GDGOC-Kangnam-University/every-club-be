package gdgoc.everyclub.auth.controller;

import gdgoc.everyclub.auth.dto.OtpResponse;
import gdgoc.everyclub.auth.dto.SendOtpRequest;
import gdgoc.everyclub.auth.dto.VerifyOtpRequest;
import gdgoc.everyclub.auth.service.OtpService;
import gdgoc.everyclub.auth.service.RateLimiterService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to email", description = "Generates and sends an OTP code to the user's email for verification")
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(
            @Valid @RequestBody SendOtpRequest request,
            HttpServletRequest httpRequest) {

        // Apply rate limiting based on client IP
        String clientIp = getClientIp(httpRequest);
        rateLimiterService.acquireOrThrow(clientIp);

        // Find user by email - always proceed regardless of existence
        // This prevents user enumeration attacks
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // Always generate and "send" OTP even if user doesn't exist
        // This prevents attackers from knowing which emails are registered
        String otpCode = otpService.generateOtp();

        if (user != null) {
            // Save verification and send email only for existing users
            otpService.saveVerification(user.getId(), otpCode);
            otpService.sendOtpEmail(request.getEmail(), otpCode);
        }
        // For non-existent users: OTP is generated but not sent, no error returned

        return ResponseEntity.ok(ApiResponse.success(
                new OtpResponse("If the email is registered, an OTP will be sent.", true)
        ));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP", description = "Verifies the OTP code and marks the user's email as verified")
    @Transactional
    public ResponseEntity<ApiResponse<OtpResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpRequest) {

        // Apply rate limiting based on client IP
        String clientIp = getClientIp(httpRequest);
        rateLimiterService.acquireOrThrow(clientIp);

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // Always return generic message to prevent user enumeration
        // Don't reveal whether the email exists or the OTP is invalid
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success(
                    new OtpResponse("Verification processing complete.", false)
            ));
        }

        // Verify OTP
        boolean isValid = otpService.verifyOtp(user.getId(), request.getCode());

        if (isValid) {
            // Mark email as verified
            user.markEmailAsVerified();
            userRepository.save(user);

            return ResponseEntity.ok(ApiResponse.success(
                    new OtpResponse("Email verified successfully.", true)
            ));
        } else {
            return ResponseEntity.ok(ApiResponse.success(
                    new OtpResponse("Verification processing complete.", false)
            ));
        }
    }

    /**
     * Extract client IP address from request, handling proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}