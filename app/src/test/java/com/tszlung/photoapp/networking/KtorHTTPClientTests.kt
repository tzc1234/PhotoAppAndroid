package com.tszlung.photoapp.networking

import com.tszlung.photoapp.helpers.*
import com.tszlung.photoapp.networking.infra.KtorHTTPClient
import com.tszlung.photoapp.util.Result
import io.ktor.client.engine.mock.*
import io.ktor.client.engine.mock.MockEngine.Companion.invoke
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.net.URL

class KtorHTTPClientTests {
    @Test
    fun `requests URL and method from engine`() = runTest {
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
    fun `fails on 5XX status codes`(statusCode: HttpStatusCode) = runTest {
        val sut = makeSUT(statusCode = statusCode)

        when (val result = sut.getFrom(anyURL())) {
            is Result.Failure -> assertEquals(HTTPClientError.SERVER_ERROR, result.error)
            is Result.Success -> fail("Should not be success")
        }
    }

    @ParameterizedTest
    @MethodSource("statusCode4xx")
    fun `fails on 4xx status codes`(statusCode: HttpStatusCode, expectError: HTTPClientError) =
        runTest {
            val sut = makeSUT(statusCode = statusCode)

            when (val result = sut.getFrom(anyURL())) {
                is Result.Failure -> assertEquals(expectError, result.error)
                is Result.Success -> fail("Should not be success")
            }
        }

    @ParameterizedTest
    @MethodSource("statusCode3xx")
    fun `fails on 3xx status codes`(statusCode: HttpStatusCode) = runTest {
        val sut = makeSUT(statusCode = statusCode)

        when (val result = sut.getFrom(anyURL())) {
            is Result.Failure -> assertEquals(HTTPClientError.UNKNOWN, result.error)
            is Result.Success -> fail("Should not be success")
        }
    }

    @Test
    fun `fails on any exception`() = runTest {
        val sut = makeSUT(statusCode = HttpStatusCode.OK, shouldThrowAnException = true)

        when (val result = sut.getFrom(anyURL())) {
            is Result.Failure -> assertEquals(HTTPClientError.UNKNOWN, result.error)
            is Result.Success -> fail("Should not be success")
        }
    }

    @ParameterizedTest
    @MethodSource("statusCode2xx")
    fun `succeeds on 2xx status codes`(statusCode: HttpStatusCode) = runTest {
        val content = "any"
        val sut = makeSUT(statusCode = statusCode, content = content)

        when (val result = sut.getFrom(anyURL())) {
            is Result.Failure -> fail("Should not be failure")
            is Result.Success -> assertEquals(content, result.data.toString(Charsets.UTF_8))
        }
    }

    companion object {
        @JvmStatic
        fun statusCode2xx(): List<Arguments> {
            return listOf(
                Arguments.of(HttpStatusCode.OK),
                Arguments.of(HttpStatusCode.ResetContent),
                Arguments.of(HttpStatusCode.MultiStatus)
            )
        }

        @JvmStatic
        fun statusCode3xx(): List<Arguments> {
            return listOf(
                Arguments.of(HttpStatusCode.MultipleChoices),
                Arguments.of(HttpStatusCode.UseProxy),
                Arguments.of(HttpStatusCode.PermanentRedirect)
            )
        }

        @JvmStatic
        fun statusCode4xx(): List<Arguments> {
            return listOf(
                Arguments.of(HttpStatusCode.Unauthorized, HTTPClientError.UNAUTHORIZED),
                Arguments.of(HttpStatusCode.NotFound, HTTPClientError.NOT_FOUND),
                Arguments.of(HttpStatusCode.RequestTimeout, HTTPClientError.TIMEOUT)
            )
        }

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
    private fun makeSUT(
        statusCode: HttpStatusCode,
        content: String = "",
        shouldThrowAnException: Boolean = false,
        requestBlock: (HttpRequestData) -> Unit = {}
    ): KtorHTTPClient {
        val mockEngine = MockEngine { request ->
            requestBlock(request)

            if (shouldThrowAnException) {
                throw RuntimeException()
            }

            respond(content, status = statusCode)
        }
        return KtorHTTPClient(mockEngine)
    }
    // endregion
}