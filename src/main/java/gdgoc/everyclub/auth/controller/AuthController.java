package gdgoc.everyclub.auth.controller;

import gdgoc.everyclub.auth.dto.LoginRequest;
import gdgoc.everyclub.auth.dto.SignupSendOtpRequest;
import gdgoc.everyclub.auth.dto.SignupTokenResponse;
import gdgoc.everyclub.auth.dto.SignupVerifyOtpRequest;
import gdgoc.everyclub.auth.service.RateLimiterService;
import gdgoc.everyclub.auth.service.SignupService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.common.exception.AuthErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.security.dto.TokenInfo;
import gdgoc.everyclub.security.jwt.JwtProvider;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final SignupService signupService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/signup/send-otp")
    @Operation(summary = "회원가입 OTP 발송", description = "학교 이메일로 OTP를 발송합니다. 이미 가입된 이메일이면 거부됩니다.")
    public ResponseEntity<ApiResponse<Void>> signupSendOtp(
            @Valid @RequestBody SignupSendOtpRequest request,
            HttpServletRequest httpRequest) {

        rateLimiterService.acquireOrThrow(getClientIp(httpRequest));
        signupService.sendOtp(request.email());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/signup/verify-otp")
    @Operation(summary = "회원가입 OTP 검증", description = "OTP를 검증하고 회원가입 진행용 signupToken을 발급합니다.")
    public ResponseEntity<ApiResponse<SignupTokenResponse>> signupVerifyOtp(
            @Valid @RequestBody SignupVerifyOtpRequest request,
            HttpServletRequest httpRequest) {

        rateLimiterService.acquireOrThrow(getClientIp(httpRequest));
        SignupTokenResponse response = signupService.verifyOtp(request.email(), request.code());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT access token을 발급합니다.")
    public ResponseEntity<ApiResponse<TokenInfo>> login(
            @Valid @RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new LogicException(AuthErrorCode.INVALID_CREDENTIALS));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new LogicException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        CustomUserDetails principal = new CustomUserDetails(
                user.getId(), user.getEmail(), "", user.getRole().name());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        TokenInfo tokenInfo = jwtProvider.generateToken(auth);
        return ResponseEntity.ok(ApiResponse.success(tokenInfo));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}