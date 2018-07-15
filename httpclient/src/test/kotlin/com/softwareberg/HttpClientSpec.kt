package com.softwareberg

import com.softwareberg.HttpMethod.GET
import com.softwareberg.HttpMethod.POST
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response

class HttpClientSpec {

    companion object {
        val httpClient = SimpleHttpClient.create()
        private val mockServerClient = startClientAndServer(1080)

        @AfterAll
        @JvmStatic
        fun `it should close http client`() {
            httpClient.close()
        }
    }

    @Test
    fun `it should do GET request`() {
        // given
        mockServerClient.`when`(request().withMethod("GET").withPath("/get")).respond(response().withStatusCode(200).withBody("OK"))

        // when
        val (statusCode, _, body) = httpClient.execute(HttpRequest(GET, "http://localhost:1080/get")).join()

        // then
        assertThat(statusCode).isEqualTo(200)
        assertThat(body).isEqualTo("OK")
    }

    @Test
    fun `it should do GET request with headers`() {
        // given
        mockServerClient.`when`(request().withMethod("GET").withPath("/get").withHeader("Accept", "text/plain").withHeader("Accept-Charset", "utf-8")).respond(response().withStatusCode(200).withHeader("Content-Type", "text/plain; utf-8").withBody("OK"))

        // when
        val (statusCode, headers, body) = httpClient.execute(HttpRequest(GET, "http://localhost:1080/get", listOf(HttpHeader("Accept", "text/plain"), HttpHeader("Accept-Charset", "utf-8")))).join()

        // then
        assertThat(statusCode).isEqualTo(200)
        assertThat(body).isEqualTo("OK")
        assertThat(headers).contains(HttpHeader("Content-Type", "text/plain; utf-8"))
    }

    @Test
    fun `it should do POST request`() {
        // given
        mockServerClient.`when`(request().withMethod("POST").withPath("/post").withBody("request")).respond(response().withStatusCode(201).withBody("created"))
        val httpClient = SimpleHttpClient.create()

        // when
        val (statusCode, _, body) = httpClient.execute(HttpRequest(POST, "http://localhost:1080/post", emptyList(), "request")).join()

        // then
        assertThat(statusCode).isEqualTo(201)
        assertThat(body).isEqualTo("created")
    }

    @BeforeEach
    fun resetBeforeTest() {
        mockServerClient.reset()
    }
}
