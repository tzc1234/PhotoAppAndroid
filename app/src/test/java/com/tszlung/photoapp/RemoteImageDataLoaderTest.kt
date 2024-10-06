package com.tszlung.photoapp

import com.tszlung.photoapp.helpers.HTTPClientSpy
import com.tszlung.photoapp.networking.HTTPClient
import com.tszlung.photoapp.util.Result
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RemoteImageDataLoaderTest {
    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `loader does not notify client upon init`() {
        val anyData = "any".toByteArray(Charsets.UTF_8)
        val client = HTTPClientSpy(Result.Success(anyData))
        @Suppress("unused") val sut = RemoteImageDataLoader(client)

        assertTrue(client.messages.isEmpty())
    }
}

class RemoteImageDataLoader(private val client: HTTPClient)