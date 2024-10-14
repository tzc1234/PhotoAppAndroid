package com.tszlung.photoapp.networking

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.helpers.*
import com.tszlung.photoapp.util.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URL

class RemoteImageDataLoaderTests {
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

        when (val result = sut.loadFrom(anyURL())) {
            is Result.Failure -> assertEquals(RemoteImageDataLoader.LoaderError.CONNECTIVITY, result.error)
            is Result.Success -> fail("should not be success")
        }
    }

    @Test
    fun `delivers invalid data error on client empty data`() = runBlocking {
        val emptyData = ByteArray(size = 0)
        val (sut, _) = makSUT(stub = Result.Success(emptyData))

        when (val result = sut.loadFrom(anyURL())) {
            is Result.Failure -> assertEquals(RemoteImageDataLoader.LoaderError.INVALID_DATA, result.error)
            is Result.Success -> fail("should not be success")
        }
    }

    @Test
    fun `delivers data on received client data`() = runBlocking {
        val data = anyData()
        val (sut, _) = makSUT(stub = Result.Success(data))

        when (val result = sut.loadFrom(anyURL())) {
            is Result.Failure -> fail("should not be failure")
            is Result.Success -> assertEquals(data, result.data)
        }
    }

    // region Helpers
    private fun makSUT(stub: Result<ByteArray, Error> = Result.Success(anyData())): Pair<ImageDataLoader, HTTPClientSpy> {
        val client = HTTPClientSpy(stub)
        return Pair(RemoteImageDataLoader(client), client)
    }
    // endregion
}