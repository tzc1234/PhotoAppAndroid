package com.tszlung.photoapp.networking

import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.features.PhotosLoader
import java.net.URL

class RemotePhotosLoader(private val client: HTTPClient, private val url: URL) : PhotosLoader {
    enum class LoaderError : Error {
        CONNECTIVITY,
        INVALID_DATA
    }

    override suspend fun load(): Result<List<Photo>, Error> {
        return when (val result = client.getFrom(url)) {
            is Result.Failure -> Result.Failure(LoaderError.CONNECTIVITY)
            is Result.Success -> PhotoResponseMapper.map(result.data)
        }
    }
}