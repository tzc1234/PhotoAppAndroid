package com.tszlung.photoapp.networking

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.features.ImageDataLoaderError
import com.tszlung.photoapp.util.*
import java.net.URL

class RemoteImageDataLoader(private val client: HTTPClient) : ImageDataLoader {
    override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        return when(val result = client.getFrom(url)) {
            is Result.Failure -> Result.Failure(ImageDataLoaderError.CONNECTIVITY)
            is Result.Success -> {
                if (result.data.isEmpty()) {
                    Result.Failure(ImageDataLoaderError.INVALID_DATA)
                } else {
                    Result.Success(result.data)
                }
            }
        }
    }
}