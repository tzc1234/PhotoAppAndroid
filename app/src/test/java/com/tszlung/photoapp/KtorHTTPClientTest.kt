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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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

    @ParameterizedTest
    @MethodSource("statusCodes5xx")
    fun `fails on 5XX status code`(statusCode: HttpStatusCode) = runBlocking {
        val sut = makeSUT(statusCode = statusCode)

        when(val result = sut.getFrom(anyURL())) {
            is Result.Failure -> assertEquals(HTTPClientError.SERVER_ERROR, result.error)
            is Result.Success -> fail("Should not be success")
        }
    }

    companion object {
        @JvmStatic
        fun statusCodes5xx(): List<Arguments> {
            return listOf(
                Arguments.of(HttpStatusCode.InternalServerError),
                Arguments.of(HttpStatusCode.NotImplemented),
                Arguments.of(HttpStatusCode.BadGateway),
                Arguments.of(HttpStatusCode.ServiceUnavailable),
                Arguments.of(HttpStatusCode.GatewayTimeout),
                Arguments.of(HttpStatusCode.VersionNotSupported),
                Arguments.of(HttpStatusCode.VariantAlsoNegotiates),
                Arguments.of(HttpStatusCode.InsufficientStorage)
            )
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
            in 500..599 -> Result.Failure(HTTPClientError.SERVER_ERROR)
            else -> Result.Failure(HTTPClientError.UNKNOWN)
        }
    }
}