package com.tszlung.photoapp.composables

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

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