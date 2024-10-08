package com.tszlung.photoapp.caching

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

        when (val result = sut.retrieveDataFor(url)) {
            is Result.Failure -> fail("should not be failure")
            is Result.Success -> assertNull(result.data)
        }
    }
}

class LruImageDataStore : ImageDataStore {
    override suspend fun retrieveDataFor(url: URL): Result<ByteArray?, Error> {
        return Result.Success(null)
    }

    override suspend fun insert(
        data: ByteArray,
        url: URL
    ): Result<Unit, Error> {
        TODO("Not yet implemented")
    }
}