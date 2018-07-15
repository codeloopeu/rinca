package com.softwareberg

import io.netty.handler.codec.http.DefaultHttpHeaders
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.Request
import org.asynchttpclient.RequestBuilder
import org.asynchttpclient.Response
import java.util.concurrent.CompletableFuture

interface HttpClient : AutoCloseable {
    fun execute(request: HttpRequest): CompletableFuture<HttpResponse>
}

class SimpleHttpClient(private val asyncHttpClient: AsyncHttpClient) : HttpClient {

    companion object {
        fun create(): HttpClient = SimpleHttpClient(DefaultAsyncHttpClient())
    }

    override fun execute(request: HttpRequest): CompletableFuture<HttpResponse> {
        return asyncHttpClient
            .prepareRequest(toAsyncRequest(request))
            .execute()
            .toCompletableFuture()
            .thenApply(this::mapToResponse)
    }

    private fun toAsyncRequest(request: HttpRequest): Request {
        val headers = request.headers.fold(DefaultHttpHeaders(), { httpHeaders, (name, value) ->
            httpHeaders.add(name, value)
            httpHeaders
        })
        val requestBuilder = RequestBuilder()
            .setUrl(request.url)
            .setMethod(request.method.name)
            .setHeaders(headers)
        if (request.body != null) {
            requestBuilder.setBody(request.body)
        }
        return requestBuilder.build()
    }

    private fun mapToResponse(response: Response): HttpResponse {
        val body: String? = response.responseBody
        val statusCode = response.statusCode
        val headers = response.headers.map { header -> HttpHeader(header.key, header.value) }
        return HttpResponse(statusCode, headers, body)
    }

    override fun close() {
        asyncHttpClient.close()
    }
}
