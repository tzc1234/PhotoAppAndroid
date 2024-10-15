package com.tszlung.photoapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.helpers.AnyError
import com.tszlung.photoapp.helpers.anyData
import com.tszlung.photoapp.helpers.makePhoto
import com.tszlung.photoapp.presentation.helpers.MainCoroutineExtension
import com.tszlung.photoapp.util.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URL

@ExtendWith(MainCoroutineExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PhotoDetailViewModelTests {
    @Test
    fun `init all properties properly upon creation`() {
        val photo = makePhoto(0)
        val (sut, loader) = makeSUT(photo)

        assertEquals(photo.author, sut.author)
        assertEquals(photo.webURL, sut.webURL)
        assertFalse(sut.isLoading)
        assertTrue(loader.requestURLs.isEmpty())
    }

    @Test
    fun `loadImage requests url from loader`() = runTest {
        val photo = makePhoto(0)
        val (sut, loader) = makeSUT(photo)

        sut.loadImage()
        advanceUntilIdle()

        assertEquals(listOf(photo.imageURL), loader.requestURLs)
    }

    @Test
    fun `loadImage delivers loading state properly`() = runTest {
        val (sut, _) = makeSUT(
            stubs = mutableListOf(
                Result.Success(anyData()),
                Result.Failure(AnyError.ANY)
            )
        )

        sut.loadImage()
        assertTrue(sut.isLoading, "Expect loading after 1st loadImage called")

        advanceUntilIdle()
        assertFalse(sut.isLoading, "Expect not loading after loader success")

        sut.loadImage()
        assertTrue(sut.isLoading, "Expect loading after 2nd loadImage called")

        advanceUntilIdle()
        assertFalse(sut.isLoading, "Expect not loading after loader failure")
    }

    @Test
    fun `loadImage delivers null image on loader failure`() = runTest {
        val (sut, _) = makeSUT(stubs = mutableListOf(Result.Failure(AnyError.ANY)))

        sut.loadImage()
        assertNull(sut.image)

        advanceUntilIdle()
        assertNull(sut.image)
    }

    @Test
    fun `loadImage delivers image on loader success`() = runTest {
        val image = anyData()
        val (sut, _) = makeSUT(stubs = mutableListOf(Result.Success(image)))

        sut.loadImage()
        assertNull(sut.image)

        advanceUntilIdle()
        assertEquals(image, sut.image)
    }

    // region Helpers
    private fun makeSUT(
        photo: Photo = makePhoto(0),
        stubs: MutableList<Result<ByteArray, Error>> = mutableListOf(Result.Failure(AnyError.ANY))
    ): Pair<PhotoDetailViewModel<ByteArray>, ImageDataLoaderSpy> {
        val loader = ImageDataLoaderSpy(stubs)
        val sut = PhotoDetailViewModel(photo, loader, { it })
        return Pair(sut, loader)
    }

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

class PhotoDetailViewModel<I>(
    private val photo: Photo,
    private val loader: ImageDataLoader,
    private val imageConvertor: (ByteArray) -> I?
) :
    ViewModel() {
    val author: String
        get() = photo.author
    val webURL: URL
        get() = photo.webURL
    var isLoading by mutableStateOf(false)
        private set
    var image by mutableStateOf<I?>(null)
        private set

    fun loadImage() {
        isLoading = true
        viewModelScope.launch {
            when (val result = loader.loadFrom(photo.imageURL)) {
                is Result.Failure -> image = null
                is Result.Success -> image = imageConvertor(result.data)
            }

            isLoading = false
        }
    }
}