package com.tszlung.photoapp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URL
import com.tszlung.photoapp.features.Error
import com.tszlung.photoapp.features.Result

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

        assertEquals(client.messages, listOf(url))
    }

    @Test
    fun `delivers connectivity error on client's error`() {
        val (sut, _) = makeSUT()

        when(val result = sut.load()) {
            is Result.Failure -> assertEquals(result.error, RemotePhotoLoader.LoaderError.CONNECTIVITY)
            is Result.Success -> fail("should not be success here")
        }
    }

    // region Helpers
    private fun makeSUT(url: URL = URL("https://any-url.com")): Pair<RemotePhotoLoader, HTTPClientSpy> {
        val client = HTTPClientSpy(error = HTTPClientSpy.HTTPClientError.ANY)
        val sut = RemotePhotoLoader(client = client, url = url)
        return Pair(sut, client)
    }
    // endregion
}

class RemotePhotoLoader(private val client: HTTPClientSpy, private val url: URL) {
    enum class LoaderError: Error {
        CONNECTIVITY
    }

    fun load(): Result<Unit, Error> {
        return when(val result = client.getFor(url)) {
            is Result.Failure -> Result.Failure(LoaderError.CONNECTIVITY)
            is Result.Success -> Result.Success(Unit)
        }
    }
}

class HTTPClientSpy(private val error: Error) {
    enum class HTTPClientError: Error {
        ANY
    }

    val messages = mutableListOf<URL>()

    fun getFor(url: URL): Result<Unit, Error> {
        messages.add(url)
        return Result.Failure(error)
    }
}