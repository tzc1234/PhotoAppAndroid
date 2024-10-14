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
        val data = "data from primary".toByteArray(Charsets.UTF_8)
        val sut = makeSUT(Result.Success(data))

        val result = sut.loadFrom(anyURL())

        assertEquals(Result.Success<ByteArray, Error>(data), result)
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

    private class ImageDataLoaderStub(private val stub: Result<ByteArray, Error>) :
        ImageDataLoader {
        override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
            return stub
        }
    }
    // endregion
}

class ImageDataLoaderWithFallbackComposite(
    private val primary: ImageDataLoader,
    fallback: ImageDataLoader
) : ImageDataLoader {
    override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        return primary.loadFrom(url)
    }
}