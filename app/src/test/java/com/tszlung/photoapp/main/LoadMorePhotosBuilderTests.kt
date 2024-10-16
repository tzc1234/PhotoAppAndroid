package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.helpers.AnyError
import com.tszlung.photoapp.helpers.anyURL
import com.tszlung.photoapp.helpers.makePhoto
import com.tszlung.photoapp.util.*
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest
import java.net.URL

class LoadMorePhotosBuilderTests {
    @Test
    fun `does not notify loadPhotos upon creation`() {
        val (_, loadPhotos) = makeSUT()

        assertTrue(loadPhotos.requestURL.isEmpty())
    }

    @Test
    fun `build delivers loadMorePhotos requests url on loadPhotos`() = runTest {
        val baseURL = URL("https://base-url.com/v1/list")
        val (sut, loadPhotos) = makeSUT(baseURL)
        val page = 2

        val loadMorePhotos = sut.build(page)
        loadMorePhotos()

        assertEquals(
            listOf(URL("https://base-url.com/%2Fv1%2Flist?page=$page")),
            loadPhotos.requestURL
        )
    }

    @Test
    fun `loadMorePhotos delivers error on loadPhotos failure`() = runTest {
        val expectedResult = Result.Failure<List<Photo>, Error>(AnyError.ANY)
        val (sut, _) = makeSUT(stub = expectedResult)

        val loadMorePhotos = sut.build(1)
        val result = loadMorePhotos()

        assertEquals(expectedResult, result)
    }

    @Test
    fun `loadMorePhotos delivers photos on loadPhotos success`() = runTest {
        val expectedResult = Result.Success<List<Photo>, Error>(listOf(makePhoto(0), makePhoto(1)))
        val (sut, _) = makeSUT(stub = expectedResult)

        val loadMorePhotos = sut.build(1)
        val result = loadMorePhotos()

        assertEquals(expectedResult, result)
    }

    // region Helpers
    private fun makeSUT(
        baseURL: URL = anyURL(),
        stub: Result<List<Photo>, Error> = Result.Failure(AnyError.ANY)
    ): Pair<LoadMorePhotosBuilder, LoadPhotosSpy> {
        val loadPhotos = LoadPhotosSpy(stub)
        val sut = LoadMorePhotosBuilder(baseURL, loadPhotos::load)
        return Pair(sut, loadPhotos)
    }

    private class LoadPhotosSpy(val stub: Result<List<Photo>, Error>) {
        val requestURL = mutableListOf<URL>()

        fun load(url: URL): Result<List<Photo>, Error> {
            requestURL.add(url)
            return stub
        }
    }
    // endregion
}

class LoadMorePhotosBuilder(
    private val baseURL: URL,
    private val loadPhotos: suspend (URL) -> Result<List<Photo>, Error>
) {
    fun build(page: Int): suspend () -> Result<List<Photo>, Error> {
        val urlBuilder = URLBuilder(
            protocol = URLProtocol(name = baseURL.protocol, defaultPort = baseURL.port),
            host = baseURL.host,
            pathSegments = listOf(baseURL.path),
            parameters = Parameters.build { append("page", page.toString()) }
        )
        return { loadPhotos(makeURL(page)) }
    }

    private fun makeURL(page: Int): URL {
        val urlBuilder = URLBuilder(
            protocol = URLProtocol(name = baseURL.protocol, defaultPort = baseURL.port),
            host = baseURL.host,
            pathSegments = listOf(baseURL.path),
            parameters = Parameters.build { append("page", page.toString()) }
        )
        return URL(urlBuilder.buildString())
    }
}