package gdgoc.everyclub.student

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StudentVerificationClientTest {
	
	@Test
	fun `check should return true for a valid school domain`() = runBlocking {
		// Arrange
		val mockEngine = MockEngine { request ->
			// Check if the request URL matches the expected URL for a valid domain
			if (request.url.toString().endsWith("/kr/ac/gachon.txt")) {
				respond(
					content = ByteReadChannel("gachon.ac.kr"),
					status = HttpStatusCode.OK,
					headers = headersOf(HttpHeaders.ContentType, "application/vnd.github.raw+json")
				)
			} else {
				// Respond with 404 for any other unexpected URL
				respond(
					content = ByteReadChannel("Not Found"),
					status = HttpStatusCode.NotFound
				)
			}
		}
		val client = StudentVerificationClient(client = io.ktor.client.HttpClient(mockEngine))
		
		// Act
		val isStudentDomain = client.check("gachon.ac.kr")
		
		// Assert
		assertTrue(isStudentDomain, "Expected gachon.ac.kr to be a valid student domain")
	}
	
	@Test
	fun `check should return true for a valid domain with real credential`() = runBlocking {
		val domain = "kangnam.ac.kr"
		val client = StudentVerificationClient()
		assertTrue { client.check(domain) }
	}
	
	@Test
	fun `check should return false for an invalid school domain with real credential`() = runBlocking {
		val domain = "asdf"
		val client = StudentVerificationClient()
		assertFalse { client.check(domain) }
	}
	
	@Test
	fun `check should return false for an invalid domain`() = runBlocking {
		// Arrange
		val mockEngine = MockEngine {
			// The mock engine will respond with 404 for any request by default if not configured
			respond(
				content = ByteReadChannel("Not Found"),
				status = HttpStatusCode.NotFound
			)
		}
		val client = StudentVerificationClient(client = io.ktor.client.HttpClient(mockEngine))
		
		// Act & Assert
		assertFalse(client.check("example.com"), "Expected example.com to be an invalid domain")
	}
}