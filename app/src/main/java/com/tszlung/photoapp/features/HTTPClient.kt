package com.tszlung.photoapp.features

import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import java.net.URL

enum class HTTPClientError : Error {
    UNKNOWN
}

interface HTTPClient {
    suspend fun getFor(url: URL): Result<ByteArray, Error>
}