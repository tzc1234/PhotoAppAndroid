package com.tszlung.photoapp.presentation

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.helpers.AnyError
import com.tszlung.photoapp.helpers.anyData
import com.tszlung.photoapp.helpers.makePhoto
import com.tszlung.photoapp.presentation.helpers.MainCoroutineExtension
import com.tszlung.photoapp.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

        assertEquals(photo, sut.photo)
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

    @Test
    fun `loadImage delivers previous image on loader failure`() = runTest {
        val image = anyData()
        val (sut, _) = makeSUT(
            stubs = mutableListOf(
                Result.Success(image),
                Result.Failure(AnyError.ANY)
            )
        )

        sut.loadImage()
        advanceUntilIdle()
        assertEquals(image, sut.image)

        sut.loadImage()
        advanceUntilIdle()
        assertEquals(image, sut.image)
    }

    @Test
    fun `delivers should reload image on loader failure`() = runTest {
        val (sut, _) = makeSUT(stubs = mutableListOf(Result.Failure(AnyError.ANY)))

        sut.loadImage()
        assertFalse(sut.shouldReloadImage)

        advanceUntilIdle()
        assertTrue(sut.shouldReloadImage)
    }

    @Test
    fun `does not deliver should reload image on image reload success`() = runTest {
        val (sut, _) = makeSUT(
            stubs = mutableListOf(
                Result.Failure(AnyError.ANY),
                Result.Success(anyData())
            )
        )

        sut.loadImage()
        advanceUntilIdle()
        assertTrue(sut.shouldReloadImage)

        sut.loadImage()
        advanceUntilIdle()
        assertFalse(sut.shouldReloadImage)
    }

    // region Helpers
    private fun makeSUT(
        photo: Photo = makePhoto(0),
        stubs: MutableList<Result<ByteArray, Error>> = mutableListOf(Result.Failure(AnyError.ANY))
    ): Pair<PhotoDetailViewModel<ByteArray>, ImageDataLoaderSpy> {
        val loader = ImageDataLoaderSpy(stubs)
        val sut = PhotoDetailViewModel(photo, loader) { it }
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