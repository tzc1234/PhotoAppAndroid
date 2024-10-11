package com.tszlung.photoapp.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tszlung.photoapp.features.ImageDataLoader
import com.tszlung.photoapp.util.Result
import kotlinx.coroutines.launch
import java.net.URL

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
                is Result.Failure -> Unit
                is Result.Success -> imageData = result.data
            }

            isLoading = false
        }
    }
}