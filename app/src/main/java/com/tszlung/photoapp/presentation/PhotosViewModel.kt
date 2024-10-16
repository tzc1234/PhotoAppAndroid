package com.tszlung.photoapp.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.presentation.util.Paginated
import com.tszlung.photoapp.presentation.util.PageablePhotosLoader
import com.tszlung.photoapp.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PhotosViewModel(private val loader: PageablePhotosLoader) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var paginatedPhotos by mutableStateOf<Paginated<List<Photo>>>(Paginated(listOf(), null))
        private set
    var errorMessage by mutableStateOf<String?>(null)
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
                    val allPhotos = if (page == 1) newPhotos else paginatedPhotos.value + newPhotos
                    paginatedPhotos = Paginated(
                        value = allPhotos,
                        loadMore = if (newPhotos.isEmpty()) null else {
                            { loadPhotos(page + 1) }
                        }
                    )
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