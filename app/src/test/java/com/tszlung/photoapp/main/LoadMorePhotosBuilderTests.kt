package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.helpers.AnyError
import com.tszlung.photoapp.helpers.anyURL
import com.tszlung.photoapp.helpers.makePhoto
import com.tszlung.photoapp.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest
import java.net.URL
import java.net.URLDecoder

class PageablePhotosLoaderAdapterTests {
    @Test
    fun `does not notify loadPhotos upon creation`() {
        val (_, loadPhotos) = makeSUT()

        assertTrue(loadPhotos.requestURL.isEmpty())
    }

    @Test
    fun `requests url on loadPhotos`() = runTest {
        val baseURL = URL("https://base-url.com/v1/list")
        val (sut, loadPhotos) = makeSUT(baseURL)
        val page = 2

        sut.loadPhotos(page)
        val decodedURLs = loadPhotos.requestURL.map {
            URL(URLDecoder.decode(it.toString(), Charsets.UTF_8))
        }

        assertEquals(
            listOf(URL("https://base-url.com/v1/list?page=$page")),
            decodedURLs
        )
    }

    @Test
    fun `delivers error on loadPhotos failure`() = runTest {
        val expectedResult = Result.Failure<List<Photo>, Error>(AnyError.ANY)
        val (sut, _) = makeSUT(stub = expectedResult)

        val result = sut.loadPhotos(1)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `delivers photos on loadPhotos success`() = runTest {
        val expectedResult = Result.Success<List<Photo>, Error>(listOf(makePhoto(0), makePhoto(1)))
        val (sut, _) = makeSUT(stub = expectedResult)

        val result = sut.loadPhotos(1)

        assertEquals(expectedResult, result)
    }

    // region Helpers
    private fun makeSUT(
        baseURL: URL = anyURL(),
        stub: Result<List<Photo>, Error> = Result.Failure(AnyError.ANY)
    ): Pair<PageablePhotosLoaderAdapter, LoadPhotosSpy> {
        val loadPhotos = LoadPhotosSpy(stub)
        val sut = PageablePhotosLoaderAdapter(baseURL, loadPhotos::load)
        return Pair(sut, loadPhotos)
    }

    private class LoadPhotosSpy(private val stub: Result<List<Photo>, Error>) {
        val requestURL = mutableListOf<URL>()

        fun load(url: URL): Result<List<Photo>, Error> {
            requestURL.add(url)
            return stub
        }
    }
    // endregion
}