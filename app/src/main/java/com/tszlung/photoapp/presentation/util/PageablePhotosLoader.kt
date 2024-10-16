package com.tszlung.photoapp.presentation.util

import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.util.*

interface PageablePhotosLoader {
    suspend fun loadPhotos(page: Int): Result<List<Photo>, Error>
}