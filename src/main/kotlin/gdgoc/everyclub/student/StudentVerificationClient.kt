package gdgoc.everyclub.student

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.*
import io.ktor.http.headers

/**
 * Check if the email domain is school-issued or not.
 */
class StudentVerificationClient(
	private val githubToken: String? = null, private val client: HttpClient = HttpClient(CIO)
) {
	/**
	 * @param domain the URL of the domain to check. For example, asdf@asdf.ac.kr's domain is 'asdf.ac.kr'.
	 * @see <a href="https://github.com/JetBrains/swot">Repository for domain data</a>
	 * @see <a href="https://docs.github.com/ko/rest/repos/contents?apiVersion=2022-11-28#get-repository-content">Github doc</a>
	 *
	 */
	suspend fun check(domain: String): Boolean {
		val path = domain.split(".").reversed()
		val endpoint = "https://api.github.com/repos/JetBrains/swot/contents/lib/domains/${path.joinToString("/")}.txt"
		val resp = client.get(endpoint) {
			headers {
				append("X-GitHub-Api-Version", "2022-11-28")
			}
			if (githubToken != null) bearerAuth(githubToken)
			accept(ContentType("application", "vnd.github.raw+json"))
		}
		return resp.status.value == 200
	}
}