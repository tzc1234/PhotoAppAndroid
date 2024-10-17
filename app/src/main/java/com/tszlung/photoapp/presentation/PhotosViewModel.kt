package com.tszlung.photoapp.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface PageablePhotosLoader {
    suspend fun loadPhotos(page: Int): Result<List<Photo>, Error>
}

class PhotosViewModel(private val loader: PageablePhotosLoader) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var photos by mutableStateOf<List<Photo>>(listOf())
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var loadMore: (() -> Unit)? = null
        private set

    companion object {
        const val ERROR_MESSAGE = "Error occurred, please try again."
    }

    fun loadPhotos(page: Int = 1) {
        isLoading = true
        viewModelScope.launch {
            when (val result = loader.loadPhotos(page)) {
                is Result.Failure -> errorMessage = ERROR_MESSAGE
                is Result.Success -> {
                    val newPhotos = result.data
                    photos = if (page == 1) newPhotos else photos + newPhotos
                    loadMore = if (newPhotos.isEmpty()) null else {
                        { loadPhotos(page + 1) }
                    }

                    errorMessage = null
                }
            }

            delay(100L)
            isLoading = false
        }
    }

    fun resetErrorMessage() {
        errorMessage = null
    }
}