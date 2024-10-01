package com.tszlung.photoapp.networking

import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import java.net.URL

enum class HTTPClientError : Error {
    UNKNOWN,
    SERVER_ERROR
}

interface HTTPClient {
    suspend fun getFrom(url: URL): Result<ByteArray, Error>
}