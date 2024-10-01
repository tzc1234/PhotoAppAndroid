package com.tszlung.photoapp.networking

import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.features.HTTPClient
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.features.PhotoLoader
import com.tszlung.photoapp.util.Result
import java.net.URL

class RemotePhotoLoader(private val client: HTTPClient, private val url: URL) : PhotoLoader {
    enum class LoaderError : Error {
        CONNECTIVITY,
        INVALID_DATA
    }

    override suspend fun load(): Result<List<Photo>, Error> {
        return when (val result = client.getFor(url)) {
            is Result.Failure -> Result.Failure(LoaderError.CONNECTIVITY)
            is Result.Success -> PhotoResponseMapper.map(result.data)
        }
    }
}