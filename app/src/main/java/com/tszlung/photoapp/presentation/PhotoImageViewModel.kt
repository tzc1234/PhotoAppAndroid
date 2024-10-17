package com.tszlung.photoapp.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.launch
import java.net.URL

class PhotoImageViewModel<I>(
    private val loader: ImageDataLoader,
    private val imageURL: URL,
    private val imageConverter: (ByteArray) -> I?
) : ViewModel() {
    var image by mutableStateOf<I?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var shouldReload by mutableStateOf(false)

    fun loadImage() {
        isLoading = true
        viewModelScope.launch {
            when (val result = loader.loadFrom(imageURL)) {
                is Result.Failure -> shouldReload = true
                is Result.Success -> image = imageConverter(result.data)
            }

            isLoading = false
        }
    }
}