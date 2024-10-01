package com.tszlung.photoapp.features

interface PhotoLoader {
    suspend fun load(): Result<List<Photo>, Error>
}