package com.tszlung.photoapp.caching

import com.tszlung.photoapp.features.ImageDataCache
import com.tszlung.photoapp.util.*
import com.tszlung.photoapp.features.ImageDataLoader
import java.net.URL

class LocalImageDataLoader(private val store: ImageDataStore) : ImageDataLoader, ImageDataCache {
    enum class LoaderError : Error {
        DATA_NOT_FOUND,
        FAILED
    }

    override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        return when (val result = store.retrieveDataFor(url)) {
            is Result.Failure -> Result.Failure(LoaderError.FAILED)
            is Result.Success -> {
                if (result.data != null) {
                    Result.Success(result.data)
                } else {
                    Result.Failure(LoaderError.DATA_NOT_FOUND)
                }
            }
        }
    }

    enum class SaveError : Error {
        FAILED
    }

    override suspend fun save(data: ByteArray, url: URL): Result<Unit, Error> {
        return when (store.insert(data, url)) {
            is Result.Failure -> Result.Failure(SaveError.FAILED)
            is Result.Success -> Result.Success(Unit)
        }
    }
}