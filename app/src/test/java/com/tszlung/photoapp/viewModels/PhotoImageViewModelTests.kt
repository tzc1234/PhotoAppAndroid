package com.tszlung.photoapp.viewModels

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.helpers.anyData
import com.tszlung.photoapp.helpers.anyURL
import com.tszlung.photoapp.util.*
import com.tszlung.photoapp.viewModels.helpers.MainCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URL

@ExtendWith(MainCoroutineExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PhotoImageViewModelTests {
    @Test
    fun `init view model successfully`() {
        val (sut, _) = makeSUT()

        assertNull(sut.imageData)
        assertFalse(sut.isLoading)
    }

    @Test
    fun `loadImageData delivers isLoading properly on loader failure`() = runTest {
        val (sut, _) = makeSUT(mutableListOf(Result.Failure(LoaderError.ANY)))

        sut.loadImageData()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `loadImageData delivers isLoading properly on loader success`() = runTest {
        val (sut, _) = makeSUT(mutableListOf(Result.Success(anyData())))

        sut.loadImageData()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `loadImageData requests with URL on loader`() = runTest {
        val url = anyURL()
        val (sut, loader) = makeSUT(mutableListOf(Result.Success(anyData())), url)

        sut.loadImageData()
        assertTrue(loader.requestURLs.isEmpty())

        advanceUntilIdle()
        assertEquals(listOf(url), loader.requestURLs)
    }

    @Test
    fun `loadImageData delivers null image data on loader failure`() = runTest {
        val (sut, _) = makeSUT(mutableListOf(Result.Failure(LoaderError.ANY)))

        sut.loadImageData()
        assertNull(sut.imageData)

        advanceUntilIdle()
        assertNull(sut.imageData)
    }

    @Test
    fun `loadImageData delivers image data on loader success`() = runTest {
        val data = anyData()
        val (sut, _) = makeSUT(mutableListOf(Result.Success(data)))

        sut.loadImageData()
        assertNull(sut.imageData)

        advanceUntilIdle()
        assertEquals(data, sut.imageData)
    }

    // region Helpers
    private fun makeSUT(
        stubs: MutableList<Result<ByteArray, Error>> = mutableListOf(),
        imageURL: URL = anyURL()
    ): Pair<PhotoImageViewModel, ImageDataLoaderSpy> {
        val imageDataLoader = ImageDataLoaderSpy(stubs)
        return Pair(PhotoImageViewModel(imageDataLoader, imageURL), imageDataLoader)
    }

    private enum class LoaderError : Error {
        ANY
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