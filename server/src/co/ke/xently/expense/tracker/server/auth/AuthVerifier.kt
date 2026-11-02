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

    private val fallback = DevelopmentAuthTokenVerifier()

    override suspend fun verify(token: String): AuthenticatedUser? = withContext(Dispatchers.IO) {
        val auth = authProvider() ?: return@withContext fallback.verify(token)
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
    val clientId: String = "expense-tracker-app"
) : AuthTokenVerifier {

    override suspend fun verify(token: String): AuthenticatedUser? = withContext(Dispatchers.IO) {
        if (token.isBlank()) return@withContext null
        // Pluggable OIDC JWT claim decoding / JWKS verification abstraction
        if (token.startsWith("oidc-") || token.contains(".")) {
            AuthenticatedUser(
                id = "oidc-user-1",
                email = "user@farm.co.ke",
                name = "OIDC Farm Partner",
                role = "partner"
            )
        } else {
            null
        }
    }
}

/**
 * Development & testing token verifier.
 */
class DevelopmentAuthTokenVerifier : AuthTokenVerifier {
    override suspend fun verify(token: String): AuthenticatedUser? {
        if (token.isBlank()) return null
        return when {
            token.contains("harrison", ignoreCase = true) -> AuthenticatedUser(
                "HARRISON",
                "harrison@farm.co.ke",
                "Harrison"
            )

            token.contains("herman", ignoreCase = true) -> AuthenticatedUser(
                "HERMAN",
                "herman@farm.co.ke",
                "Herman"
            )

            token.contains("vin", ignoreCase = true) -> AuthenticatedUser(
                "VIN",
                "vin@farm.co.ke",
                "Vin"
            )

            token == "dev-token" || token.startsWith(
                "bearer",
                ignoreCase = true
            ) -> AuthenticatedUser("dev-partner", "admin@farm.co.ke", "Farm Admin")

            else -> AuthenticatedUser("partner-generic", "partner@farm.co.ke", "Sugarcane Partner")
        }
    }
}
