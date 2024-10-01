package com.tszlung.photoapp

import com.tszlung.photoapp.networking.HTTPClient
import com.tszlung.photoapp.networking.HTTPClientError
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.mock.*
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.net.URL

class KtorHTTPClientTest {
    @Test
    fun `requests URL and method from engine`() = runBlocking {
        val url = URL("https://a-url.com")
        val loggedMessages = mutableListOf<Pair<String, HttpMethod>>()
        val mockEngine = MockEngine { request ->
            loggedMessages.add(Pair(request.url.toString(), request.method))
            respond("")
        }
        val sut = KtorHTTPClient(engine = mockEngine)

        sut.getFrom(url)

        assertEquals(listOf(Pair(url.toString(), HttpMethod.Get)), loggedMessages)
    }
}

class KtorHTTPClient(engine: HttpClientEngine = CIO.create()) : HTTPClient {
    private val client = HttpClient(engine)

    override suspend fun getFrom(url: URL): Result<ByteArray, Error> {
        client.get(url)
        return Result.Failure(HTTPClientError.UNKNOWN)
    }
}