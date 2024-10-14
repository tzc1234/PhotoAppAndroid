package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.ImageDataCache
import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import java.net.URL

class ImageDataLoaderWithCacheDecorator(
    private val loader: ImageDataLoader,
    private val cache: ImageDataCache
) : ImageDataLoader {
    override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        return when (val result = loader.loadFrom(url)) {
            is Result.Failure -> result
            is Result.Success -> {
                cache.save(result.data, url)
                result
            }
        }
    }
}