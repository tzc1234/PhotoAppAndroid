package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.helpers.AnyError
import com.tszlung.photoapp.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL

class LoadMorePhotosBuilderTests {
    @Test
    fun `does not notify loader upon creation`() {
        val loadPhotos = LoadPhotosSpy()
        val sut = LoadMorePhotosBuilder(loadPhotos::load)

        assertTrue(loadPhotos.requestURL.isEmpty())
    }

    // region Helpers
    private class LoadPhotosSpy(val stub: Result<List<Photo>, Error> = Result.Failure(AnyError.ANY)) {
        val requestURL = mutableListOf<URL>()

        fun load(url: URL): Result<List<Photo>, Error> {
            requestURL.add(url)
            return stub
        }
    }
    // endregion
}

class LoadMorePhotosBuilder(private val loadPhotos: suspend (URL) -> Result<List<Photo>, Error>)