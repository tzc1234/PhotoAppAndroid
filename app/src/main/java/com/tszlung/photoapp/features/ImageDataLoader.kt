package com.tszlung.photoapp.features

import com.tszlung.photoapp.util.*
import java.net.URL

enum class ImageDataLoaderError : Error {
    CONNECTIVITY,
    INVALID_DATA
}

interface ImageDataLoader {
    suspend fun loadFrom(url: URL): Result<ByteArray, Error>
}