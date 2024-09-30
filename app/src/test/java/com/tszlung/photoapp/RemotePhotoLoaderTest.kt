package com.tszlung.photoapp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL

class RemotePhotoLoaderTest {
    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `does not notify client upon init`() {
        val (sut, client) = makeSUT()

        assertTrue(client.messages.isEmpty())
    }

    @Test
    fun `request URL from client`() {
        val url = URL("https://a-url.com")
        val (sut, client) = makeSUT(url = url)

        sut.load()

        assertEquals(client.messages, listOf(url))
    }

    // region Helpers
    private fun makeSUT(url: URL = URL("https://any-url.com")): Pair<RemotePhotoLoader, HTTPClientSpy> {
        val client = HTTPClientSpy()
        val sut = RemotePhotoLoader(client = client, url = url)
        return Pair(sut, client)
    }
    // endregion
}

class RemotePhotoLoader(private val client: HTTPClientSpy, private val url: URL) {
    fun load() {
        client.getFor(url)
    }
}

class HTTPClientSpy {
    val messages = mutableListOf<URL>()

    fun getFor(url: URL) {
        messages.add(url)
    }
}