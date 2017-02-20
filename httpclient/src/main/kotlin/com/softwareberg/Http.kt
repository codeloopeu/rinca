package com.softwareberg

import java.nio.charset.StandardCharsets
import java.util.*

enum class HttpMethod {
    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
}

data class UsernameWithPassword(val username: String, val password: String)
data class HttpHeader(val name: String, val value: String) {
    companion object {
        fun basicAuth(username: String, password: String): HttpHeader {
            if (username.contains(':')) throw IllegalArgumentException("Username cannot have ':' [username=$username]")
            val encoded = Base64.getEncoder().encodeToString("$username:$password".toByteArray(StandardCharsets.UTF_8))
            return HttpHeader("Authorization", "Basic $encoded")
        }

        fun basicAuth(authHeaderValue: String): UsernameWithPassword {
            if (!authHeaderValue.startsWith("Basic ")) throw IllegalArgumentException("Wrong value of Basic Auth header [authHeaderValue=$authHeaderValue]")
            val withoutBasic = authHeaderValue.removePrefix("Basic ")
            val decode = try {
                Base64.getDecoder().decode(withoutBasic.toByteArray(StandardCharsets.UTF_8)).toString(StandardCharsets.UTF_8)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Wrong value of Basic Auth header [authHeaderValue=$authHeaderValue]", e)
            }
            val usernameWithPassword = decode.split(':', limit = 2)

            val username = usernameWithPassword[0]
            val password = usernameWithPassword.getOrNull(1) ?: throw IllegalArgumentException("Wrong value of Basic Auth header [authHeaderValue=$authHeaderValue]")
            return UsernameWithPassword(username, password)
        }
    }
}

data class HttpRequest(val method: HttpMethod, val url: String, val headers: List<HttpHeader> = emptyList(), val body: String? = null)
data class HttpResponse(val statusCode: Int, val headers: List<HttpHeader> = emptyList(), val body: String? = null)
