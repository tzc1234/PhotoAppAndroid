package com.tszlung.photoapp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RemotePhotoLoaderTest {
    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `does not notify client upon init`() {
        val client = HTTPClientSpy()
        val sut = RemotePhotoLoader(client = client)

        assertTrue(client.messages.isEmpty())
    }
}

class RemotePhotoLoader(private val client: HTTPClientSpy) {

}

class HTTPClientSpy {
    val messages = mutableListOf<Any>()
}