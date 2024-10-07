package com.tszlung.photoapp.features

import com.tszlung.photoapp.util.*
import java.net.URL

interface ImageDataLoader {
    suspend fun loadFrom(url: URL): Result<ByteArray, Error>
}