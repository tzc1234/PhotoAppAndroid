package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.helpers.anyURL
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL

class ImageDataLoaderWithCacheDecoratorTests {
    @Test
    fun `dose not notify loader upon init`() = runTest {
        val (_, loader) = makeSUT()

        assertTrue(loader.requestURLs.isEmpty())
    }

    @Test
    fun `requests data with URL from loader`() = runTest {
        val (sut, loader) = makeSUT()
        val url = anyURL()

        sut.loadFrom(url)

        assertEquals(listOf(url), loader.requestURLs)
    }

    // region Helpers
    private fun makeSUT(): Pair<ImageDataLoaderWithCacheDecorator, ImageDataLoaderSpy> {
        val loader = ImageDataLoaderSpy()
        val sut = ImageDataLoaderWithCacheDecorator(loader)
        return Pair(sut, loader)
    }

    private enum class LoaderError : Error {
        ANY
    }

    private class ImageDataLoaderSpy : ImageDataLoader {
        val requestURLs = mutableListOf<URL>()

        override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
            requestURLs.add(url)
            return Result.Failure(LoaderError.ANY)
        }
    }
    // endregion
}

class ImageDataLoaderWithCacheDecorator(private val loader: ImageDataLoader) : ImageDataLoader {
    override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        return loader.loadFrom(url)
    }
}