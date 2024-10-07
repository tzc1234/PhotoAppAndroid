package com.tszlung.photoapp.caching

import com.tszlung.photoapp.caching.helpers.ImageDataStoreSpy
import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.helpers.anyData
import com.tszlung.photoapp.helpers.anyURL
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import com.tszlung.photoapp.util.*
import kotlinx.coroutines.runBlocking

class CacheImageDataUseCaseTests {
    @Test
    fun `does not notify the store upon init`() {
        val (_, store) = makeSUT()

        assertTrue(store.messages.isEmpty())
    }

    @Test
    fun `save data with url to store`() = runBlocking {
        val (sut, store) = makeSUT()
        val data = anyData()
        val url = anyURL()

        sut.save(data = data, url = url)

        assertEquals(listOf(ImageDataStoreSpy.Message.Insertion(data, url)), store.messages)
    }

    // region Helpers
    private fun makeSUT(
        stub: Result<ByteArray?, Error> = Result.Success(null)
    ): Pair<LocalImageDataLoader, ImageDataStoreSpy> {
        val store = ImageDataStoreSpy(stub)
        val sut = LocalImageDataLoader(store = store)
        return Pair(sut, store)
    }
    // endregion
}