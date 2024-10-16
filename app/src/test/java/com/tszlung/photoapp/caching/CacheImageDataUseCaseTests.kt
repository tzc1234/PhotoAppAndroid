package com.tszlung.photoapp.caching

import com.tszlung.photoapp.caching.helpers.ImageDataStoreSpy
import com.tszlung.photoapp.helpers.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import com.tszlung.photoapp.util.*
import kotlinx.coroutines.test.runTest

class CacheImageDataUseCaseTests {
    @Test
    fun `does not notify the store upon init`() {
        val (_, store) = makeSUT()

        assertTrue(store.messages.isEmpty())
    }

    @Test
    fun `delivers failed error on store error after cache insertion`() = runTest {
        val (sut, _) = makeSUT(insertionStub = Result.Failure(ImageDataStoreSpy.StoreError.ANY_ERROR))

        when (val result = sut.save(data = anyData(), url = anyURL())) {
            is Result.Failure -> assertEquals(
                LocalImageDataLoader.SaveError.FAILED,
                result.error
            )

            is Result.Success -> fail("should not be success")
        }
    }

    @Test
    fun `save data with url to store`() = runTest {
        val (sut, store) = makeSUT()
        val data = anyData()
        val url = anyURL()

        sut.save(data = data, url = url)

        assertEquals(listOf(ImageDataStoreSpy.Message.Insertion(data, url)), store.messages)
    }

    // region Helpers
    private fun makeSUT(
        insertionStub: Result<Unit, Error> = Result.Success(Unit)
    ): Pair<LocalImageDataLoader, ImageDataStoreSpy> {
        val store = ImageDataStoreSpy(insertionStub = insertionStub)
        val sut = LocalImageDataLoader(store = store)
        return Pair(sut, store)
    }
    // endregion
}