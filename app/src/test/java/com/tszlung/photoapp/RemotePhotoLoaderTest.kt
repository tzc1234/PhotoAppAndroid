package com.tszlung.photoapp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URL
import com.tszlung.photoapp.features.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RemotePhotoLoaderTest {
    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `does not notify client upon init`() {
        val (sut, client) = makeSUT()

        assertTrue(client.messages.isEmpty())
    }

    @Test
    fun `requests URL from client`() {
        val url = URL("https://a-url.com")
        val (sut, client) = makeSUT(url = url)

        sut.load()

        assertEquals(listOf(url), client.messages)
    }

    @Test
    fun `delivers connectivity error on client's error`() {
        val (sut, _) = makeSUT(stub = Result.Failure(HTTPClientError.UNKNOWN))

        when (val result = sut.load()) {
            is Result.Failure -> assertEquals(
                RemotePhotoLoader.LoaderError.CONNECTIVITY,
                result.error
            )

            is Result.Success -> fail("should not be success here")
        }
    }

    @Test
    fun `delivers invalid data error on invalid data`() {
        val invalidData = "invalid".toByteArray(Charsets.UTF_8)
        val (sut, _) = makeSUT(stub = Result.Success(invalidData))

        when (val result = sut.load()) {
            is Result.Failure -> assertEquals(
                RemotePhotoLoader.LoaderError.INVALID_DATA,
                result.error
            )

            is Result.Success -> fail("should not be success here")
        }
    }

    // region Helpers
    private fun makeSUT(
        url: URL = URL("https://any-url.com"),
        stub: Result<ByteArray, Error> = Result.Failure(HTTPClientError.UNKNOWN)
    ): Pair<RemotePhotoLoader, HTTPClientSpy> {
        val client = HTTPClientSpy(stub)
        val sut = RemotePhotoLoader(client = client, url = url)
        return Pair(sut, client)
    }
    // endregion
}

class RemotePhotoLoader(private val client: HTTPClientSpy, private val url: URL) {
    enum class LoaderError : Error {
        CONNECTIVITY,
        INVALID_DATA
    }

    fun load(): Result<Unit, Error> {
        return when (val result = client.getFor(url)) {
            is Result.Failure -> Result.Failure(LoaderError.CONNECTIVITY)
            is Result.Success -> {
                val payload = result.data.toString(Charsets.UTF_8)
                try {
                    val photoResponse = Json.decodeFromString<List<PhotoResponse>>(payload)
                } catch (e: Exception) {
                    return Result.Failure(LoaderError.INVALID_DATA)
                }

                return Result.Success(Unit)
            }
        }
    }
}

@Serializable
data class PhotoResponse(
    val id: String
)

enum class HTTPClientError : Error {
    UNKNOWN
}

class HTTPClientSpy(private val stub: Result<ByteArray, Error>) {
    val messages = mutableListOf<URL>()

    fun getFor(url: URL): Result<ByteArray, Error> {
        messages.add(url)
        return stub
    }
}