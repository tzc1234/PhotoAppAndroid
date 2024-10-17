package com.tszlung.photoapp.ui.composable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tszlung.photoapp.features.Photo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosGrid(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    photos: List<Photo>,
    onReachLastItem: (() -> Unit)? = null,
    item: @Composable (Photo) -> Unit
) {
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier) {
        LazyVerticalGrid(
            GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
        ) {
            itemsIndexed(photos) { index, photo ->
                if (onReachLastItem != null && index >= photos.count() - 1) {
                    LaunchedEffect(key1 = index) {
                        onReachLastItem()
                    }
                }
                item(photo)
            }
        }
    }
}