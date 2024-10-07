package com.tszlung.photoapp.caching

import com.tszlung.photoapp.caching.helpers.ImageDataStoreSpy
import com.tszlung.photoapp.features.ImageDataLoader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import com.tszlung.photoapp.util.*

class CacheImageDataUseCaseTests {
    @Test
    fun `does not notify the store upon init`() {
        val (_, store) = makeSUT()

        assertTrue(store.requestURLs.isEmpty())
    }

    // region Helpers
    private fun makeSUT(
        stub: Result<ByteArray?, Error> = Result.Success(null)
    ): Pair<ImageDataLoader, ImageDataStoreSpy> {
        val store = ImageDataStoreSpy(stub)
        val sut = LocalImageDataLoader(store = store)
        return Pair(sut, store)
    }
    // endregion
}