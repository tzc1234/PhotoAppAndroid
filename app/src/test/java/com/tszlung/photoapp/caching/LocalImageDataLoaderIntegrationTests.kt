package com.tszlung.photoapp.caching

import com.tszlung.photoapp.caching.infra.LruImageDataStore
import com.tszlung.photoapp.helpers.anyData
import com.tszlung.photoapp.helpers.anyURL
import com.tszlung.photoapp.util.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LocalImageDataLoaderIntegrationTests {
    @Test
    fun `delivers saved data on a separate instance`() = runTest {
        val store = LruImageDataStore()
        val loaderForSave = LocalImageDataLoader(store)
        val loaderForLoad = LocalImageDataLoader(store)
        val data = anyData()
        val url = anyURL()

        loaderForSave.save(data, url)

        assert(Result.Success(data), loaderForLoad.loadFrom(url))
    }

    @Test
    fun `overrides saved data on a separate instance`() = runTest {
        val store = LruImageDataStore()
        val loaderForFirstSave = LocalImageDataLoader(store)
        val loaderForLastSave = LocalImageDataLoader(store)
        val loaderForLoad = LocalImageDataLoader(store)
        val firstData = "first".toByteArray(Charsets.UTF_8)
        val lastData = "last".toByteArray(Charsets.UTF_8)
        val url = anyURL()

        loaderForFirstSave.save(firstData, url)
        loaderForLastSave.save(lastData, url)

        assert(Result.Success(lastData), loaderForLoad.loadFrom(url))
    }

    // region Helpers
    private fun assert(expected: Result<ByteArray?, Error>, result: Result<ByteArray?, Error>) {
        assertEquals(expected, result)
    }
    // endregion
}