package com.tszlung.photoapp.networking

import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import java.net.URL

class KtorHTTPClient(engine: HttpClientEngine = CIO.create()) : HTTPClient {
    private val client = HttpClient(engine) {
        followRedirects = false
    }

    override suspend fun getFrom(url: URL): Result<ByteArray, Error> {
        val response = try {
            client.get(url)
        } catch (e: Exception) {
            return Result.Failure(HTTPClientError.UNKNOWN)
        }

        return when (response.status.value) {
            in 200..299 -> Result.Success(response.readBytes())
            401 -> Result.Failure(HTTPClientError.UNAUTHORIZED)
            404 -> Result.Failure(HTTPClientError.NOT_FOUND)
            408 -> Result.Failure(HTTPClientError.TIMEOUT)
            in 500..599 -> Result.Failure(HTTPClientError.SERVER_ERROR)
            else -> Result.Failure(HTTPClientError.UNKNOWN)
        }
    }
}