package com.tszlung.photoapp

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
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
@Composable
fun PhotosGrid(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    photos: List<Photo>,
    item: @Composable (Photo) -> Unit
) {
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier) {
        LazyVerticalGrid(
            GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
        ) {
            items(photos) { photo ->
                item(photo)
            }
        }
    }
}

@Composable
fun PhotoCard(imageData: ByteArray?, author: String) {
    Card(
        modifier = Modifier
            .padding(6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ), elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        ), border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Box(modifier = Modifier.aspectRatio(1f)) {
            if (imageData != null) {
                val imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    .asImageBitmap()
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "photo",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }

            Column(
                modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .align(Alignment.BottomStart)
            ) {
                Box {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.5f))
                            .blur(30.dp)
                    )

                    Text(
                        text = author,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

fun makeImageBitmap(color: Int): ImageBitmap {
    val bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888)
    bitmap.eraseColor(color)
    return bitmap.asImageBitmap()
}

@Composable
fun ErrorToast(message: String?) {
    if (message != null) {
        Toast.makeText(LocalContext.current, message, Toast.LENGTH_LONG).show()
    }
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