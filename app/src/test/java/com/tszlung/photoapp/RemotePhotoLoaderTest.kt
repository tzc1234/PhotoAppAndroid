package com.tszlung.photoapp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL

class RemotePhotoLoaderTest {
    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `does not notify client upon init`() {
        val client = HTTPClientSpy()
        val url = URL("https://a-url.com")
        val sut = RemotePhotoLoader(client = client, url = url)

        assertTrue(client.messages.isEmpty())
    }

    @Test
    fun `request URL from client`() {
        val client = HTTPClientSpy()
        val url = URL("https://a-url.com")
        val sut = RemotePhotoLoader(client = client, url = url)

        sut.load()

        assertEquals(client.messages, listOf(url))
    }
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