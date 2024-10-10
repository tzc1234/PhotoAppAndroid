package com.tszlung.photoapp.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.helpers.anyData
import com.tszlung.photoapp.helpers.anyURL
import com.tszlung.photoapp.util.*
import com.tszlung.photoapp.viewModels.helpers.MainCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URL

@ExtendWith(MainCoroutineExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PhotoImageViewModelTests {
    @Test
    fun `init view model successfully`() {
        val sut = makeSUT()

        assertNull(sut.imageData)
        assertFalse(sut.isLoading)
    }

    @Test
    fun `loadImageData delivers isLoading properly on loader failure`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Failure(LoaderError.ANY)))

        sut.loadImageData()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `loadImageData delivers isLoading properly on loader success`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Success(anyData())))

        sut.loadImageData()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `loadImageData delivers null image data on loader failure`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Failure(LoaderError.ANY)))

        sut.loadImageData()
        assertNull(sut.imageData)

        advanceUntilIdle()
        assertNull(sut.imageData)
    }

    @Test
    fun `loadImageData delivers image data on loader success`() = runTest {
        val data = anyData()
        val sut = makeSUT(mutableListOf(Result.Success(data)))

        sut.loadImageData()
        assertNull(sut.imageData)

        advanceUntilIdle()
        assertEquals(data, sut.imageData)
    }

    // region Helpers
    private fun makeSUT(
        stubs: MutableList<Result<ByteArray, Error>> = mutableListOf(),
        imageURL: URL = anyURL()
    ): PhotoImageViewModel {
        val imageDataLoader = ImageDataLoaderStub(stubs)
        return PhotoImageViewModel(imageDataLoader, imageURL)
    }

    private enum class LoaderError : Error {
        ANY
    }

    private class ImageDataLoaderStub(private val stubs: MutableList<Result<ByteArray, Error>>) :
        ImageDataLoader {
        override suspend fun loadFrom(url: URL): Result<ByteArray, Error> {
            return stubs.removeFirst()
        }
    }
    // endregion
}

class PhotoImageViewModel(private val loader: ImageDataLoader, private val imageURL: URL) :
    ViewModel() {
    var imageData by mutableStateOf<ByteArray?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set

    fun loadImageData() {
        isLoading = true
        viewModelScope.launch {
            when (val result = loader.loadFrom(imageURL)) {
                is Result.Failure -> imageData = null
                is Result.Success -> imageData = result.data
            }

            isLoading = false
        }
    }
}