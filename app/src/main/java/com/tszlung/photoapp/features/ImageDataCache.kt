package com.tszlung.photoapp.features

import com.tszlung.photoapp.util.*
import java.net.URL

interface ImageDataCache {
    suspend fun save(data: ByteArray, url: URL): Result<Unit, Error>
}