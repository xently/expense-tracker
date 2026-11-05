package co.ke.xently.expense.tracker.server.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AuthenticatedUser(
    val id: String,
    val email: String,
    val name: String,
    val role: String = "partner"
)

interface AuthTokenVerifier {
    suspend fun verify(token: String): AuthenticatedUser?
}

/**
 * Primary Auth: Firebase Authentication Admin SDK JWT token verification.
 */
class FirebaseAuthTokenVerifier(
    private val authProvider: () -> FirebaseAuth? = {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }
) : AuthTokenVerifier {

    override suspend fun verify(token: String): AuthenticatedUser? = withContext(Dispatchers.IO) {
        val auth = authProvider() ?: return@withContext null
        try {
            val decodedToken = auth.verifyIdToken(token)
            AuthenticatedUser(
                id = decodedToken.uid,
                email = decodedToken.email ?: "",
                name = (decodedToken.claims["name"] as? String) ?: (decodedToken.email
                    ?: decodedToken.uid),
                role = (decodedToken.claims["role"] as? String) ?: "partner"
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Pluggable OAuth2/OIDC token verifier for Keycloak, Okta, or custom OIDC providers.
 */
class OidcAuthTokenVerifier(
    val issuer: String = "https://auth.farm.co.ke/realms/farm",
    val clientId: String = "expense-tracker-app",
    private val tokenVerifier: suspend (String) -> AuthenticatedUser? = { null }
) : AuthTokenVerifier {

    override suspend fun verify(token: String): AuthenticatedUser? = withContext(Dispatchers.IO) {
        if (token.isBlank()) null else tokenVerifier(token)
    }
}

/**
 * Development & testing token verifier.
 */
class DevelopmentAuthTokenVerifier(
    private val usersByToken: Map<String, AuthenticatedUser> = emptyMap()
) : AuthTokenVerifier {
    override suspend fun verify(token: String): AuthenticatedUser? {
        return usersByToken[token]
    }
}
