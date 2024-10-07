package com.tszlung.photoapp.caching

import com.tszlung.photoapp.util.*
import com.tszlung.photoapp.features.ImageDataLoader
import java.net.URL

class LocalImageDataLoader(private val store: ImageDataStore) : ImageDataLoader {
    enum class LoaderError : Error {
        DATA_NOT_FOUND,
        RETRIEVAL_ERROR
    }

    override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        return when (val result = store.retrieveDataFor(url)) {
            is Result.Failure -> Result.Failure(LoaderError.RETRIEVAL_ERROR)
            is Result.Success -> {
                if (result.data != null) {
                    Result.Success(result.data)
                } else {
                    Result.Failure(LoaderError.DATA_NOT_FOUND)
                }
            }
        }
    }
}