package com.tszlung.photoapp

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import com.tszlung.photoapp.helpers.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URL

class LocalImageDataLoaderTest {
    @Test
    fun `does not notify the store upon init`() {
        val (_, store) = makeSUT()

        assertTrue(store.messages.isEmpty())
    }

    @Test
    fun `delivers data not found error when no cache`() = runBlocking {
        val (sut, _) = makeSUT()
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
    private fun makeSUT(stub: Result<ByteArray?, Error> = Result.Success(anyData())): Pair<ImageDataLoader, ImageDataStoreSpy> {
        val store = ImageDataStoreSpy(stub)
        val sut = LocalImageDataLoader(store = store)
        return Pair(sut, store)
    }

    private class ImageDataStoreSpy(val stub: Result<ByteArray?, Error>) : ImageDataStore {
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