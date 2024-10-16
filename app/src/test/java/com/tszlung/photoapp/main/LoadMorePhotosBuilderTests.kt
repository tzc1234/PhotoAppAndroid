package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.helpers.AnyError
import com.tszlung.photoapp.helpers.anyURL
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

    // region Helpers
    private fun makeSUT(baseURL: URL = anyURL()): Pair<LoadMorePhotosBuilder, LoadPhotosSpy> {
        val loadPhotos = LoadPhotosSpy()
        val sut = LoadMorePhotosBuilder(baseURL, loadPhotos::load)
        return Pair(sut, loadPhotos)
    }

    private class LoadPhotosSpy(val stub: Result<List<Photo>, Error> = Result.Failure(AnyError.ANY)) {
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
    fun build(page: Int): suspend () -> Unit {
        val urlBuilder = URLBuilder(
            protocol = URLProtocol(name = baseURL.protocol, defaultPort = baseURL.port),
            host = baseURL.host,
            pathSegments = listOf(baseURL.path),
            parameters = Parameters.build { append("page", page.toString()) }
        )
        val url = URL(urlBuilder.buildString())
        return { loadPhotos(url) }
    }
}