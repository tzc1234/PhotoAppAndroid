package com.tszlung.photoapp.caching.helpers

import com.tszlung.photoapp.caching.ImageDataStore
import com.tszlung.photoapp.util.*
import java.net.URL

class ImageDataStoreSpy(
    val retrievalStub: Result<ByteArray?, Error> = Result.Success(null),
    val insertionStub: Result<Unit, Error> = Result.Success(Unit),
) : ImageDataStore {
    enum class StoreError : Error {
        ANY_ERROR
    }

    sealed interface Message {
        data class Retrieval(val url: URL) : Message
        data class Insertion(val data: ByteArray, val url: URL) : Message
    }

    val messages = mutableListOf<Message>()

    override suspend fun retrieveDataFor(url: URL): Result<ByteArray?, Error> {
        messages.add(Message.Retrieval(url))
        return retrievalStub
    }

    override suspend fun insert(data: ByteArray, url: URL): Result<Unit, Error> {
        messages.add(Message.Insertion(data, url))
        return insertionStub
    }
}