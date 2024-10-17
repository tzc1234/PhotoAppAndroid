package com.tszlung.photoapp.presentation

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.helpers.AnyError
import com.tszlung.photoapp.helpers.anyData
import com.tszlung.photoapp.helpers.anyURL
import com.tszlung.photoapp.util.*
import com.tszlung.photoapp.presentation.helpers.MainCoroutineExtension
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

        assertNull(sut.image)
        assertFalse(sut.isLoading)
    }

    @Test
    fun `loadImage delivers isLoading properly on loader failure`() = runTest {
        val (sut, _) = makeSUT(mutableListOf(Result.Failure(AnyError.ANY)))

        sut.loadImage()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `loadImage delivers isLoading properly on loader success`() = runTest {
        val (sut, _) = makeSUT(mutableListOf(Result.Success(anyData())))

        sut.loadImage()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `loadImage requests with URL on loader`() = runTest {
        val url = anyURL()
        val (sut, loader) = makeSUT(mutableListOf(Result.Success(anyData())), url)

        sut.loadImage()
        assertTrue(loader.requestURLs.isEmpty())

        advanceUntilIdle()
        assertEquals(listOf(url), loader.requestURLs)
    }

    @Test
    fun `loadImage delivers null image on loader failure`() = runTest {
        val (sut, _) = makeSUT(mutableListOf(Result.Failure(AnyError.ANY)))

        sut.loadImage()
        assertNull(sut.image)

        advanceUntilIdle()
        assertNull(sut.image)
    }

    @Test
    fun `loadImage delivers image on loader success`() = runTest {
        val data = anyData()
        val (sut, _) = makeSUT(mutableListOf(Result.Success(data)))

        sut.loadImage()
        assertNull(sut.image)

        advanceUntilIdle()
        assertEquals(data, sut.image)
    }

    @Test
    fun `loadImage delivers previous image on loader failure`() = runTest {
        val data = anyData()
        val (sut, _) = makeSUT(mutableListOf(Result.Success(data), Result.Failure(AnyError.ANY)))

        sut.loadImage()
        assertNull(sut.image)

        advanceUntilIdle()
        assertEquals(data, sut.image)

        sut.loadImage()
        advanceUntilIdle()
        assertEquals(data, sut.image)
    }

    @Test
    fun `delivers should reload after load Image failure`() = runTest {
        val (sut, _) = makeSUT(mutableListOf(Result.Failure(AnyError.ANY)))

        sut.loadImage()
        assertFalse(sut.shouldReload)

        advanceUntilIdle()
        assertTrue(sut.shouldReload)
    }

    @Test
    fun `dose not deliver should reload after reload Image success`() = runTest {
        val (sut, _) = makeSUT(
            mutableListOf(
                Result.Failure(AnyError.ANY),
                Result.Success(anyData())
            )
        )

        sut.loadImage()
        advanceUntilIdle()
        assertTrue(sut.shouldReload)

        sut.loadImage()
        advanceUntilIdle()
        assertFalse(sut.shouldReload)
    }

    // region Helpers
    private fun makeSUT(
        stubs: MutableList<Result<ByteArray, Error>> = mutableListOf(),
        imageURL: URL = anyURL()
    ): Pair<PhotoImageViewModel<ByteArray>, ImageDataLoaderSpy> {
        val imageDataLoader = ImageDataLoaderSpy(stubs)
        val sut = PhotoImageViewModel<ByteArray>(imageDataLoader, imageURL) { it }
        return Pair(sut, imageDataLoader)
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