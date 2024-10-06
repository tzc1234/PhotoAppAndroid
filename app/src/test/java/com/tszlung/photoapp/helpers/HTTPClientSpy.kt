package com.tszlung.photoapp.helpers

import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import com.tszlung.photoapp.networking.HTTPClient
import java.net.URL

class HTTPClientSpy(private val stub: Result<ByteArray, Error>) : HTTPClient {
    val messages = mutableListOf<URL>()

    override suspend fun getFrom(url: URL): Result<ByteArray, Error> {
        messages.add(url)
        return stub
    }
}