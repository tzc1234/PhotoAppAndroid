package com.tszlung.photoapp.networking

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URL
import com.tszlung.photoapp.features.*
import com.tszlung.photoapp.helpers.*
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RemotePhotoLoaderTests {
    @Test
    @Suppress("UNUSED_VARIABLE")
    fun `does not notify client upon init`() {
        val (sut, client) = makeSUT()

        assertTrue(client.messages.isEmpty())
    }

    @Test
    fun `requests URL from client`() = runTest {
        val url = URL("https://a-url.com")
        val (sut, client) = makeSUT(url = url)

        sut.load()

        assertEquals(listOf(url), client.messages)
    }

    @Test
    fun `delivers connectivity error on client's error`() = runTest {
        val (sut, _) = makeSUT(stub = Result.Failure(HTTPClientError.UNKNOWN))

        when (val result = sut.load()) {
            is Result.Failure -> assertEquals(
                RemotePhotosLoader.LoaderError.CONNECTIVITY,
                result.error
            )

            is Result.Success -> fail("Should not be success here")
        }
    }

    @Test
    fun `delivers invalid data error on invalid data`() = runTest {
        val invalidData = "invalid".toByteArray(Charsets.UTF_8)
        val (sut, _) = makeSUT(stub = Result.Success(invalidData))

        when (val result = sut.load()) {
            is Result.Failure -> assertEquals(
                RemotePhotosLoader.LoaderError.INVALID_DATA,
                result.error
            )

            is Result.Success -> fail("Should not be success here")
        }
    }

    @Test
    fun `delivers empty photos on empty photos data`() = runTest {
        val emptyPhotoData = "[]".toByteArray(Charsets.UTF_8)
        val (sut, _) = makeSUT(stub = Result.Success(emptyPhotoData))

        when (val result = sut.load()) {
            is Result.Failure -> fail("Should not be failure here")
            is Result.Success -> assertTrue(result.data.isEmpty())
        }
    }

    @Test
    fun `delivers photos on photos data`() = runTest {
        val photos = listOf(makePhoto(0), makePhoto(1), makePhoto(2))
        val (sut, _) = makeSUT(stub = Result.Success(photos.toData()))

        when (val result = sut.load()) {
            is Result.Failure -> fail("Should not be failure here")
            is Result.Success -> assertEquals(photos, result.data)
        }
    }

    // region Helpers
    private fun makeSUT(
        url: URL = anyURL(),
        stub: Result<ByteArray, Error> = Result.Failure(HTTPClientError.UNKNOWN)
    ): Pair<PhotosLoader, HTTPClientSpy> {
        val client = HTTPClientSpy(stub)
        val sut = RemotePhotosLoader(client = client, url = url)
        return Pair(sut, client)
    }

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
    // endregion
}