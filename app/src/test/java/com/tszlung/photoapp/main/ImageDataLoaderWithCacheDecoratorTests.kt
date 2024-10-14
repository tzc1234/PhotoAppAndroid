package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL

class ImageDataLoaderWithCacheDecoratorTests {
    @Test
    @Suppress("UnusedVariable")
    fun `dose not notify loader upon init`() = runTest {
        val loader = ImageDataLoaderSpy()
        @Suppress("Unused") val sut = ImageDataLoaderWithCacheDecorator(loader)

        assertTrue(loader.requestURLs.isEmpty())
    }

    // region Helpers
    private class ImageDataLoaderSpy : ImageDataLoader {
        val requestURLs = mutableListOf<URL>()

        override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
            TODO("Not yet implemented")
        }
    }
    // endregion
}

class ImageDataLoaderWithCacheDecorator(private val loader: ImageDataLoader) : ImageDataLoader {
    override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        TODO("Not yet implemented")
    }
}