package com.tszlung

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*

class LocalImageDataLoaderTest {
    @Test
    @Suppress("UnusedVariable")
    fun `does not notify the store upon init`() {
        val store = ImageDataCacheStoreSpy()
        @Suppress("Unused") val sut = LocalImageDataLoader(store = store)

        assertTrue(store.messages.isEmpty())
    }
}

class ImageDataCacheStoreSpy {
    val messages = listOf<Any>()
}

class LocalImageDataLoader(private val store: ImageDataCacheStoreSpy)