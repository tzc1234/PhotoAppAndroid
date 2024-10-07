package com.tszlung

import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import java.net.URL

class LocalImageDataLoaderTest {
    @Test
    @Suppress("UnusedVariable")
    fun `does not notify the store upon init`() {
        val store = ImageDataStoreSpy()
        @Suppress("Unused") val sut = LocalImageDataLoader(store = store)

        assertTrue(store.messages.isEmpty())
    }

    // region Helpers
    private class ImageDataStoreSpy : ImageDataStore {
        val messages = listOf<Any>()
    }
    // endregion
}

interface ImageDataStore

class LocalImageDataLoader(private val store: ImageDataStore) : ImageDataLoader {
    override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
        TODO("Not yet implemented")
    }
}