package com.tszlung.photoapp.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.presentation.util.Pageable
import com.tszlung.photoapp.presentation.util.PageablePhotosLoader
import com.tszlung.photoapp.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PhotosViewModel(private val loader: PageablePhotosLoader) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var pageablePhotos by mutableStateOf<Pageable<List<Photo>>>(Pageable(listOf(), null))
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    companion object {
        const val ERROR_MESSAGE = "Error occurred, please try again."
    }

    fun loadPhotos() {
        isLoading = true
        viewModelScope.launch {
            when (val result = loader.loadPhotos(1)) {
                is Result.Failure -> errorMessage = ERROR_MESSAGE
                is Result.Success -> {
                    pageablePhotos = Pageable(result.data, null)
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