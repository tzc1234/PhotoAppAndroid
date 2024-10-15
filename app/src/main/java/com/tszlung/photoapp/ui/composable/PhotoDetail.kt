package com.tszlung.photoapp.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.main.makeImageBitmap
import java.net.URL

@Composable
fun PhotoDetail(modifier: Modifier = Modifier, photo: Photo, imageBitmap: ImageBitmap?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .aspectRatio((photo.width / photo.height).toFloat())
                .weight(1f)
        ) {
            imageBitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                photo.author,
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )

            HyperlinkText(
                modifier = Modifier.padding(horizontal = 8.dp),
                url = photo.webURL.toString()
            )
        }
    }
}

@Composable
fun HyperlinkText(modifier: Modifier = Modifier, url: String) {
    Text(
        buildAnnotatedString {
            withLink(LinkAnnotation.Url(url, TextLinkStyles(SpanStyle(Color.Blue)))) {
                append(url.toString())
            }
        }, modifier = modifier
    )
}

@Preview(showSystemUi = true)
@Composable
fun Preview() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        PhotoDetail(
            modifier = Modifier.padding(innerPadding),
            Photo(
                id = "0",
                author = "Author 0",
                width = 300,
                height = 300,
                webURL = URL("https://www.google.com/"),
                imageURL = URL("https://url.com")
            ),
            makeImageBitmap()
        )
    }
}