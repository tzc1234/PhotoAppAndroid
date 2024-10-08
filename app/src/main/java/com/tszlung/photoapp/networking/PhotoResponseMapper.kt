package com.tszlung.photoapp.networking

import com.tszlung.photoapp.features.*
import com.tszlung.photoapp.networking.RemotePhotoLoader.LoaderError
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import kotlinx.serialization.json.Json

class PhotoResponseMapper private constructor() {
    companion object {
        fun map(data: ByteArray): Result<List<Photo>, Error> {
            val payload = data.toString(Charsets.UTF_8)
            val photosResponse = try {
                Json.decodeFromString<List<PhotoResponse>>(payload)
            } catch (_: Exception) {
                return Result.Failure(LoaderError.INVALID_DATA)
            }

            return Result.Success(photosResponse.map {
                Photo(
                    id = it.id,
                    author = it.author,
                    width = it.width,
                    height = it.height,
                    webURL = it.url,
                    imageURL = it.downloadURL
                )
            })
        }
    }
}