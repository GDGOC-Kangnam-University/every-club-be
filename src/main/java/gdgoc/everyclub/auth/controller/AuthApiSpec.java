package gdgoc.everyclub.auth.controller;

import gdgoc.everyclub.auth.dto.LoginRequest;
import gdgoc.everyclub.auth.dto.SignupSendOtpRequest;
import gdgoc.everyclub.auth.dto.SignupTokenResponse;
import gdgoc.everyclub.auth.dto.SignupVerifyOtpRequest;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.docs.AuthDocs;
import gdgoc.everyclub.docs.OpenApiExamples;
import gdgoc.everyclub.security.dto.TokenInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = AuthDocs.TAG_NAME, description = AuthDocs.TAG_DESCRIPTION)
public interface AuthApiSpec {

    @PostMapping("/signup/send-otp")
    @Operation(summary = "회원가입 OTP 발송", description = "학교 이메일로 OTP를 발송합니다. 이미 가입된 이메일이면 거부됩니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "OTP 발송 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "OTP 발송 성공 예시",
                            value = OpenApiExamples.SUCCESS_VOID_RESPONSE
                    )
            )
    ))
    @RequestBody(
            required = true,
            description = "OTP 발송 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "OTP 발송 예시",
                            value = OpenApiExamples.SIGNUP_SEND_OTP_REQUEST
                    )
            )
    )
    ResponseEntity<ApiResponse<Void>> signupSendOtp(
            SignupSendOtpRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest);

    @PostMapping("/signup/verify-otp")
    @Operation(summary = "회원가입 OTP 검증", description = "OTP를 검증하고 회원가입 진행용 signupToken을 발급합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "OTP 검증 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "OTP 검증 성공 예시",
                            value = OpenApiExamples.SIGNUP_TOKEN_RESPONSE
                    )
            )
    ))
    @RequestBody(
            required = true,
            description = "OTP 검증 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "OTP 검증 예시",
                            value = OpenApiExamples.SIGNUP_VERIFY_OTP_REQUEST
                    )
            )
    )
    ResponseEntity<ApiResponse<SignupTokenResponse>> signupVerifyOtp(
            SignupVerifyOtpRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest);

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT access token을 발급합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "로그인 성공 예시",
                            value = OpenApiExamples.LOGIN_RESPONSE
                    )
            )
    ))
    @RequestBody(
            required = true,
            description = "로그인 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "로그인 예시",
                            value = OpenApiExamples.LOGIN_REQUEST
                    )
            )
    )
    ResponseEntity<ApiResponse<TokenInfo>> login(LoginRequest request);
}
