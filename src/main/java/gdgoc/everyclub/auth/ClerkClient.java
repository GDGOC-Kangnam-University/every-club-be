package gdgoc.everyclub.auth;

import com.clerk.backend_api.Clerk;
import com.clerk.backend_api.helpers.security.AuthenticateRequest;
import com.clerk.backend_api.helpers.security.models.AuthenticateRequestOptions;
import com.clerk.backend_api.helpers.security.models.RequestState;

import java.util.List;
import java.util.Map;

public class ClerkClient {
    private final Clerk clerk;
    private final String clerkSecretKey;

    public ClerkClient(String clerkSecretKey) {
        this.clerkSecretKey = clerkSecretKey;
        this.clerk = Clerk.builder().bearerAuth(clerkSecretKey).build();
    }

    /**
     * @param cookieSession The value of `__session` cookie
     */
    public RequestState getSigninDataFromCookie(String cookieSession) {
        return AuthenticateRequest.authenticateRequest(
                Map.of("cookie", List.of("__session=" + cookieSession)),
                AuthenticateRequestOptions.Builder.withSecretKey(this.clerkSecretKey).build()
        );
    }
}
