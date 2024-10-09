package com.tszlung.photoapp.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.features.PhotosLoader
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
        val photosLoader = PhotosLoaderStub()
        val sut = PhotosViewModel(photosLoader)

        assertFalse(sut.isLoading)
        assertTrue(sut.photos.isEmpty())
        assertNull(sut.errorMessage)
    }

    @Test
    fun `loads photos delivers empty photos when received empty photos`() = runTest {
        val photosLoader = PhotosLoaderStub(Result.Success(listOf()))
        val sut = PhotosViewModel(photosLoader)

        sut.loadPhotos()
        assertTrue(sut.isLoading)
        assertTrue(sut.photos.isEmpty())

        advanceUntilIdle()

        assertFalse(sut.isLoading)
        assertTrue(sut.photos.isEmpty())
    }

    @Test
    fun `loads photos delivers error message when received error from loader`() = runTest {
        val photosLoader = PhotosLoaderStub(Result.Failure(LoaderError.ANY))
        val sut = PhotosViewModel(photosLoader)

        sut.loadPhotos()
        assertTrue(sut.isLoading)
        assertNull(sut.errorMessage)

        advanceUntilIdle()

        assertFalse(sut.isLoading)
        assertEquals("Error occurred, please try again.", sut.errorMessage)
    }

    // region Helpers
    private enum class LoaderError : Error {
        ANY
    }

    private class PhotosLoaderStub(
        private val stub: Result<List<Photo>, Error> = Result.Success(
            listOf()
        )
    ) : PhotosLoader {
        override suspend fun load(): Result<List<Photo>, Error> {
            return stub
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
                is Result.Success -> photos = result.data
            }

            isLoading = false
        }
    }
}