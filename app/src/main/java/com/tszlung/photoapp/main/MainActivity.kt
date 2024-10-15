package com.tszlung.photoapp.main

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tszlung.photoapp.caching.LocalImageDataLoader
import com.tszlung.photoapp.caching.infra.LruImageDataStore
import com.tszlung.photoapp.ui.composable.ErrorToast
import com.tszlung.photoapp.ui.composable.PhotoCard
import com.tszlung.photoapp.ui.composable.PhotosGrid
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.networking.RemoteImageDataLoader
import com.tszlung.photoapp.networking.RemotePhotosLoader
import com.tszlung.photoapp.networking.infra.KtorHTTPClient
import com.tszlung.photoapp.presentation.PhotoDetailViewModel
import com.tszlung.photoapp.ui.theme.PhotoAppTheme
import com.tszlung.photoapp.presentation.PhotoImageViewModel
import com.tszlung.photoapp.presentation.PhotosViewModel
import com.tszlung.photoapp.ui.composable.PhotoDetail
import kotlinx.serialization.Serializable
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val photosURL = URL("https://picsum.photos/v2/list")
    private val client = KtorHTTPClient()
    private val remotePhotosLoader = RemotePhotosLoader(client, photosURL)
    private val remoteImageDataLoader = RemoteImageDataLoader(client)

    private val store = LruImageDataStore()
    private val localImageDataLoader = LocalImageDataLoader(store)

    private val remoteImageDataLoaderWithCache = ImageDataLoaderWithCacheDecorator(
        remoteImageDataLoader,
        localImageDataLoader
    )
    private val imageDataLoaderWithFallback = ImageDataLoaderWithFallbackComposite(
        localImageDataLoader,
        remoteImageDataLoaderWithCache
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoAppTheme {
                val photosViewModel = viewModel<PhotosViewModel>(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return PhotosViewModel(remotePhotosLoader) as T
                        }
                    }
                )

                LaunchedEffect(key1 = Unit) {
                    photosViewModel.loadPhotos()
                }

                ErrorToast(photosViewModel.errorMessage, photosViewModel::resetErrorMessage)

                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination?.route
                        TopAppBar(
                            title = {
                                val title = currentDestination?.let {
                                    with(currentDestination) {
                                        when {
                                            contains("PhotoGridNav") -> "Photos"
                                            contains("PhotoDetailNav") -> "Photo Detail"
                                            else -> null
                                        }
                                    }
                                }
                                title?.let { Text(it) }
                            },
                            navigationIcon = {
                                if (currentDestination?.contains("PhotoGridNav") == false) {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(navController = navController, startDestination = PhotoGridNav) {
                        composable<PhotoGridNav> {
                            PhotosGrid(
                                isRefreshing = photosViewModel.isLoading,
                                onRefresh = photosViewModel::loadPhotos,
                                modifier = Modifier.padding(innerPadding),
                                photos = photosViewModel.photos,
                            ) { photo ->
                                val photoImageViewModel =
                                    viewModel<PhotoImageViewModel<ImageBitmap>>(
                                        key = photo.id,
                                        factory = object : ViewModelProvider.Factory {
                                            @Suppress("UNCHECKED_CAST")
                                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                return PhotoImageViewModel<ImageBitmap>(
                                                    imageDataLoaderWithFallback,
                                                    makePhotoURL(photo.id)
                                                ) { imageConverter(it) } as T
                                            }
                                        }
                                    )

                                LaunchedEffect(key1 = Unit) {
                                    photoImageViewModel.loadImageData()
                                }

                                PhotoCard(
                                    photoImageViewModel.image,
                                    photo.author,
                                    photoImageViewModel.isLoading,
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

                        composable<PhotoDetailNav> {
                            val nav = it.toRoute<PhotoDetailNav>()
                            val photoDetailViewModel = viewModel<PhotoDetailViewModel<ImageBitmap>>(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return PhotoDetailViewModel<ImageBitmap>(
                                            Photo(
                                                nav.id,
                                                nav.author,
                                                nav.width,
                                                nav.height,
                                                URL(nav.webURL),
                                                URL(nav.imageURL)
                                            ),
                                            loader = imageDataLoaderWithFallback
                                        ) { imageConverter(it) } as T
                                    }
                                }
                            )

                            LaunchedEffect(key1 = Unit) {
                                photoDetailViewModel.loadImage()
                            }

                            PhotoDetail(
                                modifier = Modifier.padding(innerPadding),
                                photoDetailViewModel.photo,
                                photoDetailViewModel.image
                            )
                        }
                    }
                }
            }
        }
    }

    @Serializable
    object PhotoGridNav

    @Serializable
    data class PhotoDetailNav(
        val id: String,
        val author: String,
        val width: Int,
        val height: Int,
        val webURL: String,
        val imageURL: String
    )

    private fun makePhotoURL(photoId: String): URL {
        val photoDimension = 600
        return URL("https://picsum.photos/id/$photoId/$photoDimension/$photoDimension")
    }

    private fun imageConverter(data: ByteArray): ImageBitmap? {
        return BitmapFactory.decodeByteArray(data, 0, data.size)?.asImageBitmap()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun DefaultPreview() {
    PhotoAppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopAppBar(title = { Text("Photos") }) }
        ) { innerPadding ->
            PhotosGrid(
                isRefreshing = false,
                onRefresh = {},
                modifier = Modifier.padding(innerPadding),
                photos = listOf(makePhoto(0), makePhoto(1), makePhoto(2)),
            ) { photo ->
                PhotoCard(makeImageBitmap(), photo.author, false) {}
            }
        }
    }
}

private fun makePhoto(index: Int) = Photo(
    id = index.toString(),
    author = "Author $index",
    width = index,
    height = index,
    webURL = URL("https://web-url-$index.com"),
    imageURL = URL("https://url-$index.com")
)

fun makeImageBitmap(color: Int = Color.RED): ImageBitmap {
    val bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888)
    bitmap.eraseColor(color)
    return bitmap.asImageBitmap()
}