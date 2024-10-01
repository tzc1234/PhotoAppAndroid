package com.tszlung.photoapp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URL
import com.tszlung.photoapp.features.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

class RemotePhotoLoaderTest {
    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `does not notify client upon init`() {
        val (sut, client) = makeSUT()

        assertTrue(client.messages.isEmpty())
    }

    @Test
    fun `requests URL from client`() = runBlocking {
        val url = URL("https://a-url.com")
        val (sut, client) = makeSUT(url = url)

        sut.load()

        assertEquals(listOf(url), client.messages)
    }

    @Test
    fun `delivers connectivity error on client's error`() = runBlocking {
        val (sut, _) = makeSUT(stub = Result.Failure(HTTPClientError.UNKNOWN))

        when (val result = sut.load()) {
            is Result.Failure -> assertEquals(
                RemotePhotoLoader.LoaderError.CONNECTIVITY,
                result.error
            )

            is Result.Success -> fail("Should not be success here")
        }
    }

    @Test
    fun `delivers invalid data error on invalid data`() = runBlocking {
        val invalidData = "invalid".toByteArray(Charsets.UTF_8)
        val (sut, _) = makeSUT(stub = Result.Success(invalidData))

        when (val result = sut.load()) {
            is Result.Failure -> assertEquals(
                RemotePhotoLoader.LoaderError.INVALID_DATA,
                result.error
            )

            is Result.Success -> fail("Should not be success here")
        }
    }

    @Test
    fun `delivers empty photos on empty photos data`() = runBlocking {
        val emptyPhotoData = "[]".toByteArray(Charsets.UTF_8)
        val (sut, _) = makeSUT(stub = Result.Success(emptyPhotoData))

        when (val result = sut.load()) {
            is Result.Failure -> fail("Should not be failure here")
            is Result.Success -> assertTrue(result.data.isEmpty())
        }
    }

    @Test
    fun `delivers photos on photos data`() = runBlocking {
        val photos = listOf(makePhoto(0), makePhoto(1), makePhoto(2))
        val (sut, _) = makeSUT(stub = Result.Success(photos.toData()))

        when (val result = sut.load()) {
            is Result.Failure -> fail("Should not be failure here")
            is Result.Success -> assertEquals(photos, result.data)
        }
    }

    // region Helpers
    private fun makeSUT(
        url: URL = URL("https://any-url.com"),
        stub: Result<ByteArray, Error> = Result.Failure(HTTPClientError.UNKNOWN)
    ): Pair<RemotePhotoLoader, HTTPClientSpy> {
        val client = HTTPClientSpy(stub)
        val sut = RemotePhotoLoader(client = client, url = url)
        return Pair(sut, client)
    }

    private fun makePhoto(index: Int) = Photo(
        id = index.toString(),
        author = "author$index",
        width = index,
        height = index,
        webURL = URL("https://web-url-$index.com"),
        imageURL = URL("https://url-$index.com")
    )

    private fun List<Photo>.toData(): ByteArray {
        val response = map {
            PhotoResponse(
                id = it.id,
                author = it.author,
                width = it.width,
                height = it.height,
                url = it.webURL,
                downloadURL = it.imageURL
            )
        }
        return Json.encodeToString(response).toByteArray(Charsets.UTF_8)
    }

    private class HTTPClientSpy(private val stub: Result<ByteArray, Error>) : HTTPClient {
        val messages = mutableListOf<URL>()

        override suspend fun getFor(url: URL): Result<ByteArray, Error> {
            messages.add(url)
            return stub
        }
    }
    // endregion
}

class RemotePhotoLoader(private val client: HTTPClient, private val url: URL) {
    enum class LoaderError : Error {
        CONNECTIVITY,
        INVALID_DATA
    }

    suspend fun load(): Result<List<Photo>, Error> {
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

data class Photo(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val webURL: URL,
    val imageURL: URL
)

@Serializable
data class PhotoResponse(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    @Serializable(with = URLSerializer::class)
    val url: URL,
    @SerialName("download_url")
    @Serializable(with = URLSerializer::class)
    val downloadURL: URL
)

object URLSerializer : KSerializer<URL> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("stringAsURLSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: URL) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): URL {
        return URL(decoder.decodeString())
    }
}

enum class HTTPClientError : Error {
    UNKNOWN
}

interface HTTPClient {
    suspend fun getFor(url: URL): Result<ByteArray, Error>
}