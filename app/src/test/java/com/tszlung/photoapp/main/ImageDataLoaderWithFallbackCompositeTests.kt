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

class ImageDataLoaderWithFallbackCompositeTests {
    @Test
    fun `delivers data on primary loader success`() = runTest {
        val data = "data from primary".toByteArray(Charsets.UTF_8)
        val primary = ImageDataLoaderStub(Result.Success(data))
        val fallback = ImageDataLoaderStub()
        val sut = ImageDataLoaderWithFallbackComposite(primary, fallback)

        val result = sut.loadFrom(anyURL())

        assertEquals(Result.Success<ByteArray, Error>(data), result)
    }

    // region Helpers
    private class ImageDataLoaderStub(
        private val stub: Result<ByteArray, Error> = Result.Failure(
            AnyError.ANY
        )
    ) : ImageDataLoader {
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