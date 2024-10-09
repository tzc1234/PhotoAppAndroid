package com.tszlung.photoapp.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.features.PhotosLoader
import com.tszlung.photoapp.helpers.makePhoto
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import com.tszlung.photoapp.viewModels.helpers.MainCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainCoroutineExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PhotosViewModelTests {
    @Test
    fun `init view model successfully`() {
        val sut = makeSUT()

        assertFalse(sut.isLoading)
        assertTrue(sut.photos.isEmpty())
        assertNull(sut.errorMessage)
    }

    @Test
    fun `load photos delivers isLoading correctly on loader success`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Success(listOf())))

        sut.loadPhotos()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `load photos delivers isLoading correctly on loader failure`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Success(listOf())))

        sut.loadPhotos()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `load photos delivers error message when received error from loader`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Failure(LoaderError.ANY), Result.Success(listOf())))

        sut.loadPhotos()
        assertNull(sut.errorMessage)

        advanceUntilIdle()
        assertEquals("Error occurred, please try again.", sut.errorMessage)

        sut.loadPhotos()
        advanceUntilIdle()
        assertNull(sut.errorMessage)
    }

    @Test
    fun `load photos delivers empty photos when received empty photos from loader`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Success(listOf())))

        sut.loadPhotos()
        assertTrue(sut.photos.isEmpty())

        advanceUntilIdle()
        assertTrue(sut.photos.isEmpty())
    }

    @Test
    fun `load photos delivers photos when received photos from loader`() = runTest {
        val photos = listOf(makePhoto(0), makePhoto(1), makePhoto(2))
        val sut = makeSUT(mutableListOf(Result.Success(photos)))

        sut.loadPhotos()
        assertTrue(sut.photos.isEmpty())

        advanceUntilIdle()
        assertEquals(photos, sut.photos)
    }

    // region Helpers
    private fun makeSUT(stubs: MutableList<Result<List<Photo>, Error>> = mutableListOf<Result<List<Photo>, Error>>()): PhotosViewModel {
        val photosLoader = PhotosLoaderStub(stubs)
        return PhotosViewModel(photosLoader)
    }

    private enum class LoaderError : Error {
        ANY
    }

    private class PhotosLoaderStub(private val stubs: MutableList<Result<List<Photo>, Error>>) :
        PhotosLoader {
        override suspend fun load(): Result<List<Photo>, Error> {
            val result = stubs.removeFirst()
            return result
        }
    }
    // endregion
}

class PhotosViewModel(private val loader: PhotosLoader) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var photos by mutableStateOf(listOf<Photo>())
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadPhotos() {
        isLoading = true
        viewModelScope.launch {
            when (val result = loader.load()) {
                is Result.Failure -> errorMessage = "Error occurred, please try again."
                is Result.Success -> {
                    photos = result.data
                    errorMessage = null
                }
            }

            isLoading = false
        }
    }
}