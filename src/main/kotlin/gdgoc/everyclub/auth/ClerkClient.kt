package gdgoc.everyclub.auth

import com.clerk.backend_api.Clerk
import com.clerk.backend_api.helpers.security.AuthenticateRequest
import com.clerk.backend_api.helpers.security.models.AuthenticateRequestOptions
import com.clerk.backend_api.helpers.security.models.RequestState

class ClerkClient(private val clerkSecretKey: String) {
	private val sdk = Clerk.builder().bearerAuth(clerkSecretKey).build()
	
	/**
	 * @param cookieSession The value of `__session` cookie
	 */
	fun getSigninDataFromCookie(cookieSession: String): RequestState? {
		return AuthenticateRequest.authenticateRequest(
			mapOf("cookie" to listOf("__session=$cookieSession")),
			AuthenticateRequestOptions.Builder.withSecretKey(clerkSecretKey).build()
		)
	}
}