package com.tszlung.photoapp

import com.tszlung.photoapp.helpers.HTTPClientSpy
import com.tszlung.photoapp.networking.HTTPClient
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL

class RemoteImageDataLoaderTest {
    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `loader does not notify client upon init`() {
        val anyData = "any".toByteArray(Charsets.UTF_8)
        val client = HTTPClientSpy(Result.Success(anyData))
        @Suppress("unused") val sut = RemoteImageDataLoader(client)

        assertTrue(client.messages.isEmpty())
    }

    @Test
    fun `load from URL`() = runBlocking {
        val anyData = "any".toByteArray(Charsets.UTF_8)
        val client = HTTPClientSpy(Result.Success(anyData))
        val sut = RemoteImageDataLoader(client)
        val url = URL("https://a-url.com")

        sut.loadFrom(url)

        assertEquals(listOf(url), client.messages)
    }
}

class RemoteImageDataLoader(private val client: HTTPClient) {
    suspend fun loadFrom(url: URL) {
        client.getFrom(url)
    }
}