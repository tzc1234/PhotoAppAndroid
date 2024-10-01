package com.tszlung.photoapp.features

import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result

interface PhotoLoader {
    suspend fun load(): Result<List<Photo>, Error>
}