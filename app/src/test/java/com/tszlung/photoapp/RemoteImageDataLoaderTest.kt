package com.tszlung.photoapp

import com.tszlung.photoapp.helpers.HTTPClientSpy
import com.tszlung.photoapp.networking.HTTPClient
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL

class RemoteImageDataLoaderTest {
    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `loader does not notify client upon init`() {
        val (sut, client) = makSUT()

        assertTrue(client.messages.isEmpty())
    }

    @Test
    fun `load from URL`() = runBlocking {
        val (sut, client) = makSUT()
        val url = URL("https://a-url.com")

        sut.loadFrom(url)

        assertEquals(listOf(url), client.messages)
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

class RemoteImageDataLoader(private val client: HTTPClient) {
    suspend fun loadFrom(url: URL) {
        client.getFrom(url)
    }
}