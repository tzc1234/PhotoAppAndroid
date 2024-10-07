package com.tszlung.photoapp.caching.helpers

import com.tszlung.photoapp.caching.ImageDataStore
import com.tszlung.photoapp.util.*
import java.net.URL

class ImageDataStoreSpy(val stub: Result<ByteArray?, Error>) : ImageDataStore {
    enum class StoreError : Error {
        ANY_RETRIEVAL_ERROR
    }

    data class Message(val data: ByteArray, val url: URL)

    val messages = mutableListOf<Message>()
    val requestURLs = mutableListOf<URL>()

    override suspend fun retrieveDataFor(url: URL): Result<ByteArray?, Error> {
        requestURLs.add(url)
        return stub
    }

    override suspend fun insert(data: ByteArray, url: URL) {
        messages.add(Message(data, url))
    }
}