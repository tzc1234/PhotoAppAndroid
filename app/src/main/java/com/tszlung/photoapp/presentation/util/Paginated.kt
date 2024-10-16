package com.tszlung.photoapp.presentation.util

data class Paginated<T>(
    val value: T,
    val loadMore: (() -> Unit)?
)
