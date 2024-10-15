package com.tszlung.photoapp.presentation

import androidx.lifecycle.ViewModel
import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.helpers.AnyError
import com.tszlung.photoapp.helpers.makePhoto
import com.tszlung.photoapp.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL

class PhotoDetailViewModelTests {
    @Test
    fun `init all properties properly upon creation`() {
        val loader = ImageDataLoaderSpy(mutableListOf(Result.Failure(AnyError.ANY)))
        val photo = makePhoto(0)
        val sut = PhotoDetailViewModel(photo, loader)

        assertEquals(photo.author, sut.author)
        assertEquals(photo.webURL, sut.webURL)
        assertTrue(loader.requestURLs.isEmpty())
    }

    // region Helpers
    private class ImageDataLoaderSpy(private val stubs: MutableList<Result<ByteArray, Error>>) :
        ImageDataLoader {
        val requestURLs = mutableListOf<URL>()

        override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
            requestURLs.add(url)
            return stubs.removeFirst()
        }
    }
    // endregion
}

class PhotoDetailViewModel(private val photo: Photo, private val loader: ImageDataLoader) : ViewModel() {
    val author: String
        get() = photo.author
    val webURL: URL
        get() = photo.webURL
}