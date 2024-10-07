package com.tszlung.photoapp.caching

import com.tszlung.photoapp.caching.helpers.ImageDataStoreSpy
import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import com.tszlung.photoapp.helpers.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URL

class LoadCachedImageDataUseCaseTests {
    @Test
    fun `does not notify the store upon init`() {
        val (_, store) = makeSUT()

        assertTrue(store.messages.isEmpty())
    }

    @Test
    fun `requests data with url from store`() = runBlocking {
        val (sut, store) = makeSUT()
        val url = URL("https://a-url.com")

        sut.loadFrom(url)

        assertEquals(listOf(ImageDataStoreSpy.Message.Retrieval(url)), store.messages)
    }

    @Test
    fun `delivers data not found error when no cache`() = runBlocking {
        val (sut, _) = makeSUT()

        when (val result = sut.loadFrom(anyURL())) {
            is Result.Failure -> assertEquals(
                LocalImageDataLoader.LoaderError.DATA_NOT_FOUND,
                result.error
            )

            is Result.Success -> fail("should not be success")
        }
    }

    @Test
    fun `delivers failed error on store error after cache retrieval`() = runBlocking {
        val (sut, _) = makeSUT(retrievalStub = Result.Failure(ImageDataStoreSpy.StoreError.ANY_ERROR))

        when (val result = sut.loadFrom(anyURL())) {
            is Result.Failure -> assertEquals(
                LocalImageDataLoader.LoaderError.FAILED,
                result.error
            )

            is Result.Success -> fail("should not be success")
        }
    }

    @Test
    fun `delivers stored cache data`() = runBlocking {
        val data = anyData()
        val (sut, _) = makeSUT(retrievalStub = Result.Success(data))

        when (val result = sut.loadFrom(anyURL())) {
            is Result.Failure -> fail("should not be failure")
            is Result.Success -> assertEquals(data, result.data)
        }
    }

    // region Helpers
    private fun makeSUT(retrievalStub: Result<ByteArray?, Error> = Result.Success(null)): Pair<ImageDataLoader, ImageDataStoreSpy> {
        val store = ImageDataStoreSpy(retrievalStub)
        val sut = LocalImageDataLoader(store = store)
        return Pair(sut, store)
    }
    // endregion
}