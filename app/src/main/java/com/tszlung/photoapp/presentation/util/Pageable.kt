package com.tszlung.photoapp.presentation.util

data class Pageable<T>(
    val value: T,
    val loadMore: (() -> Unit)?
)
