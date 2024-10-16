package com.tszlung.photoapp.presentation

import com.tszlung.photoapp.features.Photo
import com.tszlung.photoapp.helpers.AnyError
import com.tszlung.photoapp.helpers.makePhoto
import com.tszlung.photoapp.util.Error
import com.tszlung.photoapp.util.Result
import com.tszlung.photoapp.presentation.helpers.MainCoroutineExtension
import com.tszlung.photoapp.presentation.util.PageablePhotosLoader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainCoroutineExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PhotosViewModelTests {
    @Test
    fun `init view model successfully`() {
        val sut = makeSUT()

        assertFalse(sut.isLoading)
        assertTrue(sut.pageablePhotos.value.isEmpty())
        assertNull(sut.pageablePhotos.loadMore)
        assertNull(sut.errorMessage)
    }

    @Test
    fun `load photos delivers isLoading correctly on loader success`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Success(listOf())))

        sut.loadPhotos()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `load photos delivers isLoading correctly on loader failure`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Success(listOf())))

        sut.loadPhotos()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `load photos delivers error message when received error from loader`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Failure(AnyError.ANY), Result.Success(listOf())))

        sut.loadPhotos()
        assertNull(sut.errorMessage)

        advanceUntilIdle()
        assertEquals("Error occurred, please try again.", sut.errorMessage)

        sut.loadPhotos()
        advanceUntilIdle()
        assertNull(sut.errorMessage)
    }

    @Test
    fun `set error message to null after resetErrorMessage`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Failure(AnyError.ANY)))

        sut.loadPhotos()
        advanceUntilIdle()
        assertEquals("Error occurred, please try again.", sut.errorMessage)

        sut.resetErrorMessage()
        assertNull(sut.errorMessage)
    }

    @Test
    fun `load photos delivers empty photos when received empty photos from loader`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Success(listOf())))

        sut.loadPhotos()
        assertTrue(sut.pageablePhotos.value.isEmpty())

        advanceUntilIdle()
        assertTrue(sut.pageablePhotos.value.isEmpty())
    }

    @Test
    fun `load photos delivers photos when received photos from loader`() = runTest {
        val photos = listOf(makePhoto(0), makePhoto(1), makePhoto(2))
        val sut = makeSUT(mutableListOf(Result.Success(photos)))

        sut.loadPhotos()
        assertTrue(sut.pageablePhotos.value.isEmpty())

        advanceUntilIdle()
        assertEquals(photos, sut.pageablePhotos.value)
    }

    @Test
    fun `load photos does not change previously loaded photos after an error from loader`() = runTest {
        val photos = listOf(makePhoto(0))
        val sut = makeSUT(mutableListOf(Result.Success(photos), Result.Failure(AnyError.ANY)))

        sut.loadPhotos()
        advanceUntilIdle()
        assertEquals(photos, sut.pageablePhotos.value)

        sut.loadPhotos()
        advanceUntilIdle()
        assertEquals(photos, sut.pageablePhotos.value)
    }

    @Test
    fun `loadPhotos does not delivers loadMore when received empty photos from loader`() = runTest {
        val emptyPhoto = listOf<Photo>()
        val sut = makeSUT(mutableListOf(Result.Success(emptyPhoto)))

        sut.loadPhotos()
        advanceUntilIdle()

        assertNull(sut.pageablePhotos.loadMore)
    }

    @Test
    fun `loadPhotos does not delivers loadMore on loader error`() = runTest {
        val sut = makeSUT(mutableListOf(Result.Failure(AnyError.ANY)))

        sut.loadPhotos()
        advanceUntilIdle()

        assertNull(sut.pageablePhotos.loadMore)
    }

    // region Helpers
    private fun makeSUT(stubs: MutableList<Result<List<Photo>, Error>> = mutableListOf()): PhotosViewModel {
        val photosLoader = PhotosLoaderStub(stubs)
        return PhotosViewModel(photosLoader)
    }

    private class PhotosLoaderStub(private val stubs: MutableList<Result<List<Photo>, Error>>) :
        PageablePhotosLoader {
        override suspend fun loadPhotos(page: Int): Result<List<Photo>, Error> {
            return stubs.removeFirst()
        }
    }
    // endregion
}