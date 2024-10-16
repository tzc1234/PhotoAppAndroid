package com.tszlung.photoapp.main.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.navigation.NavController
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.main.nav.PhotoDetailNav
import com.tszlung.photoapp.presentation.PhotoImageViewModel
import com.tszlung.photoapp.presentation.PhotosViewModel
import com.tszlung.photoapp.ui.composable.PhotoCard
import com.tszlung.photoapp.ui.composable.PhotosGrid

@Composable
fun PhotosScreen(
    modifier: Modifier,
    navController: NavController,
    photosViewModel: PhotosViewModel,
    photoImageViewModel: @Composable (Photo) -> PhotoImageViewModel<ImageBitmap>,
) {
    PhotosGrid(
        isRefreshing = photosViewModel.isLoading,
        onRefresh = photosViewModel::loadPhotos,
        modifier = modifier,
        photos = photosViewModel.photos,
        onReachLastItem = photosViewModel.loadMore
    ) { photo ->
        val photoImageViewModel = photoImageViewModel(photo)

        LaunchedEffect(key1 = photo.id) {
            photoImageViewModel.loadImage()
        }

        PhotoCard(
            photoImageViewModel.image,
            photo.author,
            photoImageViewModel.isLoading,
            if (photoImageViewModel.shouldReload) { photoImageViewModel::loadImage } else null
        ) {
            navController.navigate(
                PhotoDetailNav(
                    photo.id,
                    photo.author,
                    photo.width,
                    photo.height,
                    photo.webURL.toString(),
                    photo.imageURL.toString()
                )
            )
        }
    }
}