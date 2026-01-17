package gdgoc.everyclub.auth;

import com.clerk.backend_api.helpers.security.AuthenticateRequest;
import com.clerk.backend_api.helpers.security.models.AuthenticateRequestOptions;
import com.clerk.backend_api.helpers.security.models.RequestState;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ClerkClientTest {

    @Test
    void getSigninDataFromCookie() {
        // Given
        String secretKey = "test_secret_key";
        String cookieSession = "test_session_token";
        ClerkClient clerkClient = new ClerkClient(secretKey);
        
        RequestState expectedRequestState = mock(RequestState.class);

        try (MockedStatic<AuthenticateRequest> mockedAuthenticateRequest = mockStatic(AuthenticateRequest.class)) {
            mockedAuthenticateRequest.when(() -> AuthenticateRequest.authenticateRequest(
                    eq(Map.of("cookie", List.of("__session=" + cookieSession))),
                    any(AuthenticateRequestOptions.class)
            )).thenReturn(expectedRequestState);

            // When
            RequestState result = clerkClient.getSigninDataFromCookie(cookieSession);

            // Then
            assertEquals(expectedRequestState, result);
        }
    }
}
