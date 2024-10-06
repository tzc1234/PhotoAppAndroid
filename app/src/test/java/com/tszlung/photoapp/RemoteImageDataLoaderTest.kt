package com.tszlung.photoapp

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.features.ImageDataLoaderError
import com.tszlung.photoapp.helpers.HTTPClientSpy
import com.tszlung.photoapp.helpers.anyURL
import com.tszlung.photoapp.networking.HTTPClientError
import com.tszlung.photoapp.networking.RemoteImageDataLoader
import com.tszlung.photoapp.util.*
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
    fun `delivers invalid data error on client empty data`() = runBlocking {
        val emptyData = ByteArray(size = 0)
        val (sut, _) = makSUT(stub = Result.Success(emptyData))

        when(val result = sut.loadFrom(anyURL())) {
            is Result.Failure -> assertEquals(ImageDataLoaderError.INVALID_DATA, result.error)
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
    private fun makSUT(stub: Result<ByteArray, Error> = Result.Success(anyData())): Pair<ImageDataLoader, HTTPClientSpy> {
        val client = HTTPClientSpy(stub)
        return Pair(RemoteImageDataLoader(client), client)
    }

    private fun anyData(): ByteArray {
        return "any".toByteArray(Charsets.UTF_8)
    }
    // endregion
}