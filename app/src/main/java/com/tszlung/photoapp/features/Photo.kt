package com.tszlung.photoapp.features

import java.net.URL

data class Photo(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val webURL: URL,
    val imageURL: URL
)
