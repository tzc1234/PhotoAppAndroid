package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.helpers.AnyError
import com.tszlung.photoapp.helpers.anyURL
import kotlinx.coroutines.test.runTest
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.ByteArray

class ImageDataLoaderWithFallbackCompositeTests {
    @Test
    fun `delivers data on primary loader success`() = runTest {
        val expectedResult = successResult("data from primary".toByteArray(Charsets.UTF_8))
        val sut = makeSUT(expectedResult)

        val result = sut.loadFrom(anyURL())

        assertEquals(expectedResult, result)
    }

    @Test
    fun `delivers data on fallback loader when primary loader failure`() = runTest {
        val expectedResult = successResult("data from fallback".toByteArray(Charsets.UTF_8))
        val sut = makeSUT(failuresResult(), expectedResult)

        val result = sut.loadFrom(anyURL())

        assertEquals(expectedResult, result)
    }

    @Test
    fun `delivers error on both primary and fallback loader error`() = runTest {
        val expectedResult = failuresResult()
        val sut = makeSUT(failuresResult(), expectedResult)

        val result = sut.loadFrom(anyURL())

        assertEquals(expectedResult, result)
    }

    // region Helpers
    private fun makeSUT(
        primaryStub: Result<ByteArray, Error> = Result.Failure(AnyError.ANY),
        fallbackStub: Result<ByteArray, Error> = Result.Failure(AnyError.ANY)
    ): ImageDataLoaderWithFallbackComposite {
        val primary = ImageDataLoaderStub(primaryStub)
        val fallback = ImageDataLoaderStub(fallbackStub)
        return ImageDataLoaderWithFallbackComposite(primary, fallback)
    }

    private fun successResult(data: ByteArray) = Result.Success<ByteArray, Error>(data)
    private fun failuresResult() = Result.Failure<ByteArray, Error>(AnyError.ANY)

    private class ImageDataLoaderStub(private val stub: Result<ByteArray, Error>) :
        ImageDataLoader {
        override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
            return stub
        }
    }
    // endregion
}