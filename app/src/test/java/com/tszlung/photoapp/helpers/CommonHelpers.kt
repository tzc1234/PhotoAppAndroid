package com.tszlung.photoapp.helpers

import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.util.Error
import java.net.URL

fun anyURL() = URL("https://any-url.com")

fun anyData() = "any".toByteArray(Charsets.UTF_8)

fun makePhoto(index: Int) = Photo(
    id = index.toString(),
    author = "author$index",
    width = index,
    height = index,
    webURL = URL("https://web-url-$index.com"),
    imageURL = URL("https://url-$index.com")
)

enum class AnyError : Error { ANY }