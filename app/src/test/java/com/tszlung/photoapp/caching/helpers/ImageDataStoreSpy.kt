package com.tszlung.photoapp.caching.helpers

import com.tszlung.photoapp.caching.ImageDataStore
import com.tszlung.photoapp.util.*
import java.net.URL

class ImageDataStoreSpy(val stub: Result<ByteArray?, Error>) : ImageDataStore {
    enum class StoreError : Error {
        ANY_RETRIEVAL_ERROR
    }

    val requestURLs = mutableListOf<URL>()

    override suspend fun retrieveDataFor(url: URL): Result<ByteArray?, Error> {
        requestURLs.add(url)
        return stub
    }
}