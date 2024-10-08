package com.tszlung.photoapp.caching

import com.tszlung.photoapp.util.*
import java.net.URL

interface ImageDataStore {
    suspend fun retrieveDataFor(url: URL): Result<ByteArray?, Error>
    suspend fun insert(data: ByteArray, url: URL): Result<Unit, Error>
}