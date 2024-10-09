package com.tszlung.photoapp.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.features.PhotosLoader
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PhotosViewModelTests {
    @Test
    fun `init view model successfully`() {
        val photosLoader = PhotosLoaderStub()
        val sut = PhotosViewModel(photosLoader)

        assertFalse(sut.isLoading)
        assertTrue(sut.photos.isEmpty())
        assertNull(sut.errorMessage)
    }

    // region Helpers
    private class PhotosLoaderStub : PhotosLoader {
        override suspend fun load(): Result<List<Photo>, Error> {
            TODO("Not yet implemented")
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
}