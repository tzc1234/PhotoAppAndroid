package com.tszlung

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*

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

class LocalImageDataLoader(private val store: ImageDataStore)