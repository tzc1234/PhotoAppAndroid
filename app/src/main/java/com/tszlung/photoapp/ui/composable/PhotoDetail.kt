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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.main.makeImageBitmap
import com.tszlung.photoapp.ui.composable.modifiers.shimmer
import java.net.URL
import kotlin.math.max

@Composable
fun PhotoDetail(
    modifier: Modifier = Modifier,
    photo: Photo,
    imageBitmap: ImageBitmap?,
    onRetry: (() -> Unit)? = null,
    isShimming: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        Spacer(Modifier.weight(0.7f))

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondary)
                .fillMaxWidth()
                .aspectRatio(max(photo.width, 1).toFloat() / max(photo.height, 1).toFloat())
                .shimmer(isShimming)
        ) {
            imageBitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }

            onRetry?.let {
                TextButton(
                    onClick = it, modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        "↻",
                        color = Color.White,
                        fontSize = 60.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
            photo = Photo(
                id = "0",
                author = "Author 0",
                width = 300,
                height = 300,
                webURL = URL("https://www.google.com"),
                imageURL = URL("https://a-url.com")
            ),
            imageBitmap = makeImageBitmap(),
            onRetry = {},
            isShimming = false
        )
    }
}