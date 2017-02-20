package com.softwareberg

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.util.*

class HttpHeaderSpec {

    @Test
    fun `it should create basic auth header`() {
        // given
        val username = "test"
        val password = "password"
        // when
        val (name, value) = HttpHeader.basicAuth(username, password)
        // then
        assertEquals("Authorization", name)
        assertEquals("Basic dGVzdDpwYXNzd29yZA==", value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `it should not create basic auth header if username has colon`() {
        // given
        val username = "te:st"
        val password = "password"
        // when
        try {
            HttpHeader.basicAuth(username, password)
        } catch (e: Exception) {
            // then
            assertEquals("Username cannot have ':' [username=te:st]", e.message)
            throw e
        }
        fail()
    }

    @Test
    fun `it should create basic auth header if password has colon`() {
        // given
        val username = "test"
        val password = "pass:word"
        // when
        val (name, value) = HttpHeader.basicAuth(username, password)
        // then
        assertEquals("Authorization", name)
        assertEquals("Basic dGVzdDpwYXNzOndvcmQ=", value)
    }

    @Test
    fun `it should extract username and password from basic auth header`() {
        // given
        val headerValue = "Basic ${encodeText("test:password")}"
        // when
        val (username, password) = HttpHeader.basicAuth(headerValue)
        // then
        assertEquals("test", username)
        assertEquals("password", password)
    }

    @Test
    fun `it should extract username and password from basic auth header if password has colon`() {
        // given
        val headerValue = "Basic ${encodeText("test:pass:word")}"
        // when
        val (username, password) = HttpHeader.basicAuth(headerValue)
        // then
        assertEquals("test", username)
        assertEquals("pass:word", password)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `it should return an error if basic auth does not have 'Basic' prefix`() {
        // given
        val headerValue = encodeText("test:password")
        try {
            // when
            HttpHeader.basicAuth(headerValue)
        } catch (e: Exception) {
            // then
            assertEquals("Wrong value of Basic Auth header [authHeaderValue=$headerValue]", e.message)
            throw e
        }
        fail()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `it should return an error if basic auth has wrong prefix`() {
        // given
        val headerValue = "BasicAuth ${encodeText("test:password")}"
        try {
            // when
            HttpHeader.basicAuth(headerValue)
        } catch (e: Exception) {
            // then
            assertEquals("Wrong value of Basic Auth header [authHeaderValue=$headerValue]", e.message)
            throw e
        }
        fail()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `it should return an error if basic auth is not base64`() {
        // given
        val headerValue = "Basic d="
        try {
            // when
            HttpHeader.basicAuth(headerValue)
        } catch (e: Exception) {
            // then
            assertEquals("Wrong value of Basic Auth header [authHeaderValue=Basic d=]", e.message)
            throw e
        }
        fail()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `it should return an error if basic auth does not contain password`() {
        // given
        val headerValue = "Basic ${encodeText("test")}"
        try {
            // when
            HttpHeader.basicAuth(headerValue)
        } catch (e: Exception) {
            // then
            assertEquals("Wrong value of Basic Auth header [authHeaderValue=$headerValue]", e.message)
            throw e
        }
        fail()
    }

    private fun encodeText(text: String): String = Base64.getEncoder().encodeToString(text.toByteArray(StandardCharsets.UTF_8))
}
