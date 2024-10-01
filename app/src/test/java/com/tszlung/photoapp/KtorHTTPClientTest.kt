package com.tszlung.photoapp

import com.tszlung.photoapp.networking.HTTPClient
import com.tszlung.photoapp.networking.HTTPClientError
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.mock.*
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.fail
import java.net.URL

class KtorHTTPClientTest {
    @Test
    fun `requests URL and method from engine`() = runBlocking {
        val url = URL("https://a-url.com")
        val loggedMessages = mutableListOf<Pair<String, HttpMethod>>()
        val sut = makeSUT(statusCode = HttpStatusCode.OK) { request ->
            loggedMessages.add(Pair(request.url.toString(), request.method))
        }

        sut.getFrom(url)

        assertEquals(listOf(Pair(url.toString(), HttpMethod.Get)), loggedMessages)
    }

    @Test
    fun `fails on 5XX status code`() = runBlocking {
        val sut = makeSUT(statusCode = HttpStatusCode.ServiceUnavailable)

        when(val result = sut.getFrom(anyURL())) {
            is Result.Failure -> assertEquals(HTTPClientError.SERVER_ERROR, result.error)
            is Result.Success -> fail("Should not be success")
        }
    }

    // region Helpers
    private fun makeSUT(statusCode: HttpStatusCode, requestBlock: (HttpRequestData) -> Unit = {}): KtorHTTPClient {
        val mockEngine = MockEngine { request ->
            requestBlock(request)
            respond("", status = statusCode)
        }
        return KtorHTTPClient(mockEngine)
    }

    private fun anyURL() = URL("https://any-url.com")
    // endregion
}

class KtorHTTPClient(engine: HttpClientEngine = CIO.create()) : HTTPClient {
    private val client = HttpClient(engine)

    override suspend fun getFrom(url: URL): Result<ByteArray, Error> {
        val response = client.get(url)
        return when(response.status.value) {
            503 -> Result.Failure(HTTPClientError.SERVER_ERROR)
            else -> Result.Failure(HTTPClientError.UNKNOWN)
        }
    }
}