package com.tszlung.photoapp.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.launch

class PhotoDetailViewModel<I>(
    val photo: Photo,
    private val loader: ImageDataLoader,
    private val imageConvertor: (ByteArray) -> I?
) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var image by mutableStateOf<I?>(null)
        private set

    fun loadImage() {
        isLoading = true
        viewModelScope.launch {
            when (val result = loader.loadFrom(photo.imageURL)) {
                is Result.Failure -> Unit
                is Result.Success -> image = imageConvertor(result.data)
            }

            isLoading = false
        }
    }
}