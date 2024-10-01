package com.tszlung.photoapp.networking

import com.tszlung.photoapp.features.Error
import com.tszlung.photoapp.features.HTTPClient
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.features.PhotoLoader
import com.tszlung.photoapp.features.Result
import kotlinx.serialization.json.Json
import java.net.URL

class RemotePhotoLoader(private val client: HTTPClient, private val url: URL) : PhotoLoader {
    enum class LoaderError : Error {
        CONNECTIVITY,
        INVALID_DATA
    }

    override suspend fun load(): Result<List<Photo>, Error> {
        return when (val result = client.getFor(url)) {
            is Result.Failure -> Result.Failure(LoaderError.CONNECTIVITY)
            is Result.Success -> {
                val payload = result.data.toString(Charsets.UTF_8)
                try {
                    val photosResponse = Json.decodeFromString<List<PhotoResponse>>(payload)
                    val photos = photosResponse.map {
                        Photo(
                            id = it.id,
                            author = it.author,
                            width = it.width,
                            height = it.height,
                            webURL = it.url,
                            imageURL = it.downloadURL
                        )
                    }
                    return Result.Success(photos)
                } catch (e: Exception) {
                    return Result.Failure(LoaderError.INVALID_DATA)
                }
            }
        }
    }
}