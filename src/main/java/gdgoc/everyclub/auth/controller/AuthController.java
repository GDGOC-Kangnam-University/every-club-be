package gdgoc.everyclub.auth.controller;

import gdgoc.everyclub.auth.dto.OtpResponse;
import gdgoc.everyclub.auth.dto.SendOtpRequest;
import gdgoc.everyclub.auth.dto.VerifyOtpRequest;
import gdgoc.everyclub.auth.service.OtpService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to email", description = "Generates and sends an OTP code to the user's email for verification")
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success(
                    new OtpResponse("User not found. Please register first.", false)
            ));
        }

        // Generate OTP
        String otpCode = otpService.generateOtp();

        // Save verification
        otpService.saveVerification(user.getId(), otpCode);

        // Send OTP email (currently logs the OTP)
        otpService.sendOtpEmail(request.getEmail(), otpCode);

        return ResponseEntity.ok(ApiResponse.success(
                new OtpResponse("OTP sent to " + request.getEmail() + ". Check server logs for the code.", true)
        ));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP", description = "Verifies the OTP code and marks the user's email as verified")
    @Transactional
    public ResponseEntity<ApiResponse<OtpResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success(
                    new OtpResponse("User not found.", false)
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
                    new OtpResponse("Invalid or expired OTP code.", false)
            ));
        }
    }
}