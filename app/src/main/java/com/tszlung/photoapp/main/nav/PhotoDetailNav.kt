package com.tszlung.photoapp.main.nav

import com.tszlung.photoapp.features.Photo
import kotlinx.serialization.Serializable
import java.net.URL

@Serializable
data class PhotoDetailNav(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val webURL: String,
    val imageURL: String
) {
    fun toPhoto() = Photo(
        id = this.id,
        author = this.author,
        width = this.width,
        height = this.height,
        webURL = URL(this.webURL),
        imageURL = URL(this.imageURL)
    )
}
