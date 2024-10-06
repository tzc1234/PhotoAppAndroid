package com.tszlung.photoapp

import com.tszlung.photoapp.helpers.HTTPClientSpy
import com.tszlung.photoapp.helpers.anyURL
import com.tszlung.photoapp.networking.HTTPClient
import com.tszlung.photoapp.networking.HTTPClientError
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URL

class RemoteImageDataLoaderTest {
    @Test
    fun `loader does not notify client upon init`() {
        val (_, client) = makSUT()

        assertTrue(client.messages.isEmpty())
    }

    @Test
    fun `load from URL`() = runBlocking {
        val (sut, client) = makSUT()
        val url = URL("https://a-url.com")

        sut.loadFrom(url)

        assertEquals(listOf(url), client.messages)
    }

    @Test
    fun `delivers connectivity error on client error`() = runBlocking {
        val (sut, _) = makSUT(stub = Result.Failure(HTTPClientError.UNKNOWN))

        when(val result = sut.loadFrom(anyURL())) {
            is Result.Failure -> assertEquals(ImageDataLoaderError.CONNECTIVITY, result.error)
            is Result.Success -> fail("should not be success")
        }
    }

    @Test
    fun `delivers data on received client data`() = runBlocking {
        val data = anyData()
        val (sut, _) = makSUT(stub = Result.Success(data))

        when(val result = sut.loadFrom(anyURL())) {
            is Result.Failure -> fail("should not be failure")
            is Result.Success -> assertEquals(data, result.data)
        }
    }

    // region Helpers
    private fun makSUT(stub: Result<ByteArray, Error> = Result.Success(anyData())): Pair<RemoteImageDataLoader, HTTPClientSpy> {
        val client = HTTPClientSpy(stub)
        return Pair(RemoteImageDataLoader(client), client)
    }

    private fun anyData(): ByteArray {
        return "any".toByteArray(Charsets.UTF_8)
    }
    // endregion
}

enum class ImageDataLoaderError : Error {
    CONNECTIVITY
}

class RemoteImageDataLoader(private val client: HTTPClient) {
    suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        return when(val result = client.getFrom(url)) {
            is Result.Failure -> Result.Failure(ImageDataLoaderError.CONNECTIVITY)
            is Result.Success -> Result.Success(result.data)
        }
    }
}