package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.ImageDataCache
import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.helpers.*
import com.tszlung.photoapp.main.helpers.failuresResult
import com.tszlung.photoapp.main.helpers.successResult
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

    @Test
    fun `delivers error on loader failure`() = runTest {
        val expectedResult = failuresResult()
        val (sut, _) = makeSUT(expectedResult)

        val result = sut.loadFrom(anyURL())

        assertEquals(expectedResult, result)
    }

    @Test
    fun `delivers data on loader success`() = runTest {
        val expectedResult = successResult(anyData())
        val (sut, _) = makeSUT(expectedResult)

        val result = sut.loadFrom(anyURL())

        assertEquals(expectedResult, result)
    }

    @Test
    fun `does not cache data with url on loader failure`() = runTest {
        val cache = ImageDataCacheSpy()
        val (sut, _) = makeSUT(failuresResult(), cache)

        sut.loadFrom(anyURL())

        assertTrue(cache.cachedData.isEmpty())
    }

    @Test
    fun `caches data with url on loader success`() = runTest {
        val cache = ImageDataCacheSpy()
        val data = anyData()
        val url = anyURL()
        val (sut, _) = makeSUT(Result.Success(data), cache)

        sut.loadFrom(url)

        assertEquals(listOf(ImageDataCacheSpy.Cached(data, url)), cache.cachedData)
    }

    @Test
    fun `ignores error on cache error`() = runTest {
        val cache = ImageDataCacheSpy(Result.Failure(AnyError.ANY))
        val expectedResult = successResult(anyData())
        val (sut, _) = makeSUT(expectedResult, cache)

        val result = sut.loadFrom(anyURL())

        assertEquals(expectedResult, result)
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

    private class ImageDataCacheSpy(private val stub: Result<Unit, Error> = Result.Success(Unit)) : ImageDataCache {
        data class Cached(val data: ByteArray, val url: URL)

        val cachedData = mutableListOf<Any>()

        override suspend fun save(data: ByteArray, url: URL): Result<Unit, Error> {
            cachedData.add(Cached(data, url))
            return stub
        }
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