package com.tszlung.photoapp

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tszlung.photoapp.composables.ErrorToast
import com.tszlung.photoapp.composables.PhotoCard
import com.tszlung.photoapp.composables.PhotosGrid
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.networking.RemoteImageDataLoader
import com.tszlung.photoapp.networking.RemotePhotosLoader
import com.tszlung.photoapp.networking.infra.KtorHTTPClient
import com.tszlung.photoapp.ui.theme.PhotoAppTheme
import com.tszlung.photoapp.viewModels.PhotoImageViewModel
import com.tszlung.photoapp.viewModels.PhotosViewModel
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val photosURL = URL("https://picsum.photos/v2/list")
    private val client = KtorHTTPClient()
    private val remotePhotosLoader = RemotePhotosLoader(client, photosURL)
    private val remoteImageDataLoader = RemoteImageDataLoader(client)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val photosViewModel = viewModel<PhotosViewModel>(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return PhotosViewModel(remotePhotosLoader) as T
                    }
                }
            )

            PhotoAppTheme {
                ErrorToast(photosViewModel.errorMessage)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { Text("Photos") })
                    }
                ) { innerPadding ->
                    LaunchedEffect(key1 = Unit) {
                        photosViewModel.loadPhotos()
                    }

                    PhotoGridContainer(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = photosViewModel
                    ) { photo ->
                        val photoURL = makePhotoURL(photo.id)
                        val viewModel = viewModel<PhotoImageViewModel>(
                            key = photo.id,
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return PhotoImageViewModel(remoteImageDataLoader, photoURL) as T
                                }
                            }
                        )

                        LaunchedEffect(key1 = Unit) {
                            viewModel.loadImageData()
                        }

                        PhotoCard(viewModel.imageData, photo.author)
                    }
                }
            }
        }
    }

    private fun makePhotoURL(photoId: String): URL {
        val photoDimension = 600
        return URL("https://picsum.photos/id/$photoId/$photoDimension/$photoDimension")
    }
}

@Composable
fun PhotoGridContainer(
    modifier: Modifier = Modifier,
    viewModel: PhotosViewModel,
    item: @Composable (Photo) -> Unit
) {
    PhotosGrid(
        isRefreshing = viewModel.isLoading,
        onRefresh = viewModel::loadPhotos,
        modifier = modifier,
        photos = viewModel.photos,
        item = item
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun DefaultPreview() {
    PhotoAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(title = { Text("Photos") })
            }
        ) { innerPadding ->
            PhotosGrid(
                isRefreshing = false,
                onRefresh = {},
                modifier = Modifier.padding(innerPadding),
                photos = listOf(),
                item = {}
            )
        }
    }
}

fun makeImageBitmap(color: Int): ImageBitmap {
    val bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888)
    bitmap.eraseColor(color)
    return bitmap.asImageBitmap()
}