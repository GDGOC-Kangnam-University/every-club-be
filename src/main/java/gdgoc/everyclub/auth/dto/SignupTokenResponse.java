package gdgoc.everyclub.auth.dto;

public record SignupTokenResponse(
        String signupToken,
        long expiresIn
) {
}