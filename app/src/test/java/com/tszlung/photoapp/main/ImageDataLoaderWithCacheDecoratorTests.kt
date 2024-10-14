package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.helpers.*
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
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

    @Test
    fun `delivers error on loader failure`() = runTest {
        val (sut, _) = makeSUT(Result.Failure(LoaderError.ANY))

        when (val result = sut.loadFrom(anyURL())) {
            is Result.Failure -> assertEquals(LoaderError.ANY, result.error)
            is Result.Success -> fail("should not be success")
        }
    }

    @Test
    fun `delivers data on loader success`() = runTest {
        val data = anyData()
        val (sut, _) = makeSUT(Result.Success(data))

        when (val result = sut.loadFrom(anyURL())) {
            is Result.Failure -> fail("should not be failure")
            is Result.Success -> assertEquals(data, result.data)
        }
    }

    @Test
    fun `does not cache data with url on loader failure`() = runTest {
        val cache = ImageDataCacheSpy()
        val (sut, _) = makeSUT(Result.Failure(LoaderError.ANY), cache)

        sut.loadFrom(anyURL())

        assertTrue(cache.cachedData.isEmpty())
    }

    // region Helpers
    private fun makeSUT(
        stub: Result<ByteArray, Error> = Result.Success(anyData()),
        cache: ImageDataCache = ImageDataCacheSpy()
    ): Pair<ImageDataLoaderWithCacheDecorator, ImageDataLoaderSpy> {
        val loader = ImageDataLoaderSpy(stub)
        val sut = ImageDataLoaderWithCacheDecorator(loader, cache)
        return Pair(sut, loader)
    }

    private enum class LoaderError : Error {
        ANY
    }

    private class ImageDataCacheSpy : ImageDataCache {
        val cachedData = mutableListOf<Any>()
    }

    private class ImageDataLoaderSpy(private val stub: Result<ByteArray, Error>) : ImageDataLoader {
        val requestURLs = mutableListOf<URL>()

        override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
            requestURLs.add(url)
            return stub
        }
    }
    // endregion
}

interface ImageDataCache {

}

class ImageDataLoaderWithCacheDecorator(
    private val loader: ImageDataLoader,
    private val cache: ImageDataCache
) : ImageDataLoader {
    override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        return loader.loadFrom(url)
    }
}