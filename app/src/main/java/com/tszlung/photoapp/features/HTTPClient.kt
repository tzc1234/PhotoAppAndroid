package com.tszlung.photoapp.features

import java.net.URL

enum class HTTPClientError : Error {
    UNKNOWN
}

interface HTTPClient {
    suspend fun getFor(url: URL): Result<ByteArray, Error>
}