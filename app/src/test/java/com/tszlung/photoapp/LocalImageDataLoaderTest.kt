package com.tszlung.photoapp

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URL

class LocalImageDataLoaderTest {
    @Test
    @Suppress("UnusedVariable")
    fun `does not notify the store upon init`() {
        val store = ImageDataStoreSpy()
        @Suppress("Unused") val sut = LocalImageDataLoader(store = store)

        assertTrue(store.messages.isEmpty())
    }

    @Test
    fun `delivers data not found error when no cache`() = runBlocking {
        val store = ImageDataStoreSpy()
        val sut = LocalImageDataLoader(store = store)
        val url = URL("https://a-url.com")

        when (val result = sut.loadFrom(url)) {
            is Result.Failure -> assertEquals(
                LocalImageDataLoader.LoaderError.DATA_NOT_FOUND,
                result.error
            )

            is Result.Success -> fail("should not be success")
        }
    }

    // region Helpers
    private class ImageDataStoreSpy : ImageDataStore {
        val messages = listOf<Any>()
    }
    // endregion
}

interface ImageDataStore

class LocalImageDataLoader(private val store: ImageDataStore) : ImageDataLoader {
    enum class LoaderError : Error {
        DATA_NOT_FOUND
    }

    override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        return Result.Failure(LoaderError.DATA_NOT_FOUND)
    }
}