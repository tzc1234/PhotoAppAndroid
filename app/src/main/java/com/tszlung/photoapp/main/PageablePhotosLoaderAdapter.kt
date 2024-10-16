package com.tszlung.photoapp.main

import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.presentation.util.PageablePhotosLoader
import com.tszlung.photoapp.util.*
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import java.net.URL

class PageablePhotosLoaderAdapter(
    private val baseURL: URL,
    private val loadPhotos: suspend (URL) -> Result<List<Photo>, Error>
) : PageablePhotosLoader {
    override suspend fun loadPhotos(page: Int): Result<List<Photo>, Error> {
        return loadPhotos(makeURL(page))
    }

    private fun makeURL(page: Int): URL {
        val path = if (!baseURL.path.isEmpty() && baseURL.path.first() == '/') {
            baseURL.path.drop(1)
        } else {
            baseURL.path
        }
        val urlBuilder = URLBuilder(
            protocol = URLProtocol(name = baseURL.protocol, defaultPort = baseURL.port),
            host = baseURL.host,
            pathSegments = listOf(path),
            parameters = Parameters.build { append("page", page.toString()) }
        )
        return URL(urlBuilder.buildString())
    }
}