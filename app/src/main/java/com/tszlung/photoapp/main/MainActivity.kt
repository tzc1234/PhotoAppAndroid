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
import com.tszlung.photoapp.main.nav.PhotoDetailNav
import com.tszlung.photoapp.main.nav.PhotoGridNav
import com.tszlung.photoapp.main.screens.PhotoDetailScreen
import com.tszlung.photoapp.main.screens.PhotosScreen
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val photosURL = URL("https://picsum.photos/v2/list")
    private val client = KtorHTTPClient()
    private val pageablePhotosLoader = PageablePhotosLoaderAdapter(photosURL) {
        RemotePhotosLoader(client, it).load()
    }
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
                            return PhotosViewModel(pageablePhotosLoader) as T
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
                                    with(it) {
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
                                if (!(currentDestination ?: "").contains("PhotoGridNav")) {
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
                            PhotosScreen(
                                modifier = Modifier.padding(innerPadding),
                                navController = navController,
                                photosViewModel = photosViewModel
                            ) { photo ->
                                viewModel<PhotoImageViewModel<ImageBitmap>>(
                                    key = photo.id,
                                    factory = object : ViewModelProvider.Factory {
                                        @Suppress("UNCHECKED_CAST")
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return PhotoImageViewModel(
                                                imageDataLoaderWithFallback,
                                                makePhotoURL(photo.id)
                                            ) { imageConverter(it) } as T
                                        }
                                    }
                                )
                            }
                        }

                        composable<PhotoDetailNav> {
                            val nav = it.toRoute<PhotoDetailNav>()
                            val photoDetailViewModel = viewModel<PhotoDetailViewModel<ImageBitmap>>(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return PhotoDetailViewModel(
                                            photo = nav.toPhoto(),
                                            loader = imageDataLoaderWithFallback
                                        ) { imageConverter(it) } as T
                                    }
                                }
                            )

                            PhotoDetailScreen(
                                modifier = Modifier.padding(innerPadding),
                                photoDetailViewModel = photoDetailViewModel
                            )
                        }
                    }
                }
            }
        }
    }

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
                PhotoCard(
                    makeImageBitmap(),
                    photo.author,
                    false,
                    {}
                ) {}
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