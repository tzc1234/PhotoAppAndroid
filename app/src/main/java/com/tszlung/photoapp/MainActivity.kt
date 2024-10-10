package com.tszlung.photoapp

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.os.Bundle
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tszlung.photoapp.ui.theme.PhotoAppTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { Text("Photos") })
                    }
                ) { innerPadding ->
                    PhotosGrid(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun PhotosGrid(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(8.dp)
    ) {
        items(20) { item ->
            PhotoCard(makeImageBitmap(android.graphics.Color.RED), "Author")
        }
    }
}

@Composable
fun PhotoCard(imageBitmap: ImageBitmap, author: String) {
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
        Box {
            Image(
                bitmap = imageBitmap,
                contentDescription = "image",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

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
            PhotosGrid(modifier = Modifier.padding(innerPadding))
        }
    }
}