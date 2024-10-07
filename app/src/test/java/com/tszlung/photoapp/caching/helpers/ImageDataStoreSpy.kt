package com.tszlung.photoapp.caching.helpers

import com.tszlung.photoapp.caching.ImageDataStore
import com.tszlung.photoapp.util.*
import java.net.URL

class ImageDataStoreSpy(val stub: Result<ByteArray?, Error>) : ImageDataStore {
    enum class StoreError : Error {
        ANY_RETRIEVAL_ERROR
    }

    sealed interface Message {
        data class Retrieval(val url: URL) : Message
        data class Insertion(val data: ByteArray, val url: URL) : Message
    }

    val messages = mutableListOf<Message>()

    override suspend fun retrieveDataFor(url: URL): Result<ByteArray?, Error> {
        messages.add(Message.Retrieval(url))
        return stub
    }

    override suspend fun insert(data: ByteArray, url: URL) {
        messages.add(Message.Insertion(data, url))
    }
}