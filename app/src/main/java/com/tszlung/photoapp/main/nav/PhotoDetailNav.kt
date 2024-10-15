package com.tszlung.photoapp.main.nav

import kotlinx.serialization.Serializable

@Serializable
data class PhotoDetailNav(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val webURL: String,
    val imageURL: String
)
