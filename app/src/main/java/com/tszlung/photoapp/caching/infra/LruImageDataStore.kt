package com.tszlung.photoapp.caching.infra

import androidx.collection.LruCache
import com.tszlung.photoapp.caching.ImageDataStore
import com.tszlung.photoapp.util.*
import java.net.URL

class LruImageDataStore : ImageDataStore {
    private val cache = LruCache<URL, ByteArray>(maxSize = 8 * 1024 * 1024) // 8MiB

    override suspend fun retrieveDataFor(url: URL): Result<ByteArray?, Error> {
        return Result.Success(cache[url])
    }

    override suspend fun insert(
        data: ByteArray,
        url: URL
    ): Result<Unit, Error> {
        cache.put(key = url, value = data)
        return Result.Success(Unit)
    }
}