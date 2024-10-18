package com.tszlung.photoapp.ui.composable

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tszlung.photoapp.features.Photo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosGrid(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    photos: List<Photo>,
    onReachLastItem: (() -> Unit)? = null,
    gridItem: @Composable (Photo) -> Unit
) {
    val gridState = rememberLazyGridState()
    val interaction = gridState.interactionSource.interactions.collectAsStateWithLifecycle(null)

    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier) {
        val bottomReached: Boolean by remember {
            derivedStateOf {
                val cannotScrollForward = !gridState.canScrollForward
                val stopDragging = interaction.value is DragInteraction.Stop
                val lastVisibleItemIndex =
                    gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val lastVisibleItemReached =
                    lastVisibleItemIndex >= gridState.layoutInfo.totalItemsCount - 1
                lastVisibleItemIndex != 0 && lastVisibleItemReached && cannotScrollForward && stopDragging
            }
        }

        if (onReachLastItem != null) {
            LaunchedEffect(key1 = bottomReached) {
                if (bottomReached) {
                    onReachLastItem()
                }
            }
        }

        LazyVerticalGrid(
            GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(8.dp),
        ) {
            items(photos) { photo ->
                gridItem(photo)
            }
        }
    }
}