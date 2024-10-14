package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import java.net.URL

class ImageDataLoaderWithFallbackComposite(
    private val primary: ImageDataLoader,
    private val fallback: ImageDataLoader
) : ImageDataLoader {
    override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        return when (val result = primary.loadFrom(url)) {
            is Result.Failure -> fallback.loadFrom(url)
            is Result.Success -> result
        }
    }
}