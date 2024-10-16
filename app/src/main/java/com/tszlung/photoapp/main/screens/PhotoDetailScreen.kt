package com.tszlung.photoapp.main.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.tszlung.photoapp.presentation.PhotoDetailViewModel
import com.tszlung.photoapp.ui.composable.PhotoDetail

@Composable
fun PhotoDetailScreen(
    modifier: Modifier,
    photoDetailViewModel: PhotoDetailViewModel<ImageBitmap>
) {
    val photoId = photoDetailViewModel.photo.id
    LaunchedEffect(key1 = photoId) {
        photoDetailViewModel.loadImage()
    }

    PhotoDetail(
        modifier,
        photoDetailViewModel.photo,
        photoDetailViewModel.image,
        if (photoDetailViewModel.shouldReloadImage) photoDetailViewModel::loadImage else null,
        photoDetailViewModel.isLoading
    )
}