package com.tszlung.photoapp.networking

import com.tszlung.photoapp.features.*
import com.tszlung.photoapp.networking.RemotePhotoLoader.LoaderError
import kotlinx.serialization.json.Json

class PhotoResponseMapper {
    companion object {
        fun map(data: ByteArray): Result<List<Photo>, Error> {
            val payload = data.toString(Charsets.UTF_8)
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