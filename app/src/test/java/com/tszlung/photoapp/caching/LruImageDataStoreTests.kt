package com.tszlung.photoapp.caching

import androidx.collection.LruCache
import com.tszlung.photoapp.helpers.anyData
import com.tszlung.photoapp.helpers.anyURL
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URL

class LruImageDataStoreTests {
    @Test
    fun `delivers null data when no cache existed`() = runBlocking {
        val sut = LruImageDataStore()
        val url = anyURL()

        assert(Result.Success(null), sut.retrieveDataFor(url))
    }

    @Test
    fun `retrieves twice delivers null data when no cache exist, no side effects`() = runBlocking {
        val sut = LruImageDataStore()
        val url = anyURL()

        assert(Result.Success(null), sut.retrieveDataFor(url))
        assert(Result.Success(null), sut.retrieveDataFor(url))
    }

    @Test
    fun `delivers data on cache data`() = runBlocking {
        val sut = LruImageDataStore()
        val data = anyData()
        val url = anyURL()

        insert(data, url, sut)
        assert(Result.Success(data), sut.retrieveDataFor(url))
    }

    @Test
    fun `retrieves twice delivers cached data, no side effects`() = runBlocking {
        val sut = LruImageDataStore()
        val data = anyData()
        val url = anyURL()

        insert(data, url, sut)
        assert(Result.Success(data), sut.retrieveDataFor(url))
        assert(Result.Success(data), sut.retrieveDataFor(url))
    }

    // region Helpers
    private fun assert(expected: Result<ByteArray?, Error>, result: Result<ByteArray?, Error>) {
        assertEquals(expected, result)
    }

    private suspend fun insert(data: ByteArray, url: URL, sut: LruImageDataStore) {
        when (val result = sut.insert(data, url)) {
            is Result.Failure -> fail("should not be failure")
            is Result.Success -> assertEquals(Unit, result.data)
        }
    }
    // endregion
}

class LruImageDataStore : ImageDataStore {
    private val cache = LruCache<URL, ByteArray>(maxSize = 8 * 1024 * 1024) // 8MiB

    override suspend fun retrieveDataFor(url: URL): Result<ByteArray?, Error> {
        return Result.Success(cache[url])
    }

    override suspend fun insert(
        data: ByteArray,
        url: URL
    ): Result<Unit, Error> {
        cache.put(key = url, value = data)
        return Result.Success(Unit)
    }
}