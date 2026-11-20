package com.nawin.booknook.data.account

import com.nawin.booknook.domain.model.AccountError
import com.nawin.booknook.domain.model.AccountException
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Runs the real Supabase client against canned Auth responses. */
class SupabaseAccountRepositoryTest {

    private fun repository(respond: MockRequestHandleScope.(HttpRequestData) -> io.ktor.client.request.HttpResponseData) =
        SupabaseAccountRepository(
            supabaseClientOrNull(
                url = "https://test.supabase.co",
                key = "test-key",
                configureAuth = {
                    // Keep the session in memory and off the Android lifecycle, which a JVM test does not have.
                    sessionManager = MemorySessionManager()
                    codeVerifierCache = MemoryCodeVerifierCache()
                    autoLoadFromStorage = false
                    alwaysAutoRefresh = false
                    enableLifecycleCallbacks = false
                },
                configure = { httpEngine = MockEngine { request -> respond(request) } },
            ),
        )

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    @Test
    fun `signing in returns the account with the name kept in its metadata`() = runTest {
        val repository = repository { json(SESSION) }

        val account = repository.signIn("sara@example.com", "librosylibros").getOrThrow()

        assertEquals("sara@example.com", account.email)
        assertEquals("Sara", account.name)
        assertEquals(account, repository.account.first())
    }

    @Test
    fun `wrong credentials come back as an account error`() = runTest {
        val repository = repository {
            json("""{"code":400,"error_code":"invalid_credentials","msg":"Invalid login credentials"}""", HttpStatusCode.BadRequest)
        }

        val error = repository.signIn("sara@example.com", "equivocada").exceptionOrNull()

        assertEquals(AccountError.INVALID_CREDENTIALS, (error as AccountException).error)
    }

    @Test
    fun `signing out forgets the session`() = runTest {
        val repository = repository { request ->
            if (request.url.encodedPath.endsWith("logout")) respond("", HttpStatusCode.NoContent) else json(SESSION)
        }
        repository.signIn("sara@example.com", "librosylibros").getOrThrow()

        assertTrue(repository.signOut().isSuccess)
        assertNull(repository.account.first())
    }

    @Test
    fun `a build without a project has no account`() = runTest {
        val repository = SupabaseAccountRepository(supabaseClientOrNull(url = "", key = ""))

        assertFalse(repository.isAvailable)
        assertNull(repository.account.first())
        assertEquals(AccountError.NOT_AVAILABLE, (repository.signOut().exceptionOrNull() as AccountException).error)
    }

    private companion object {
        val SESSION = """
            {
              "access_token": "access",
              "token_type": "bearer",
              "expires_in": 3600,
              "refresh_token": "refresh",
              "user": {
                "id": "user-1",
                "aud": "authenticated",
                "email": "sara@example.com",
                "user_metadata": { "name": "Sara" }
              }
            }
        """.trimIndent()
    }
}
