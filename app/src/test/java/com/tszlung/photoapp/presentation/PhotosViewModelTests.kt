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
        val (sut, _) = makeSUT()

        assertFalse(sut.isLoading)
        assertTrue(sut.paginatedPhotos.value.isEmpty())
        assertNull(sut.paginatedPhotos.loadMore)
        assertNull(sut.errorMessage)
    }

    @Test
    fun `load photos delivers isLoading correctly on loader success`() = runTest {
        val (sut, _) = makeSUT(mutableListOf(Result.Success(listOf())))

        sut.loadPhotos()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `load photos delivers isLoading correctly on loader failure`() = runTest {
        val (sut, _) = makeSUT(mutableListOf(Result.Success(listOf())))

        sut.loadPhotos()
        assertTrue(sut.isLoading)

        advanceUntilIdle()
        assertFalse(sut.isLoading)
    }

    @Test
    fun `load photos delivers error message when received error from loader`() = runTest {
        val (sut, _) = makeSUT(
            mutableListOf(
                Result.Failure(AnyError.ANY),
                Result.Success(listOf())
            )
        )

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
        val (sut, _) = makeSUT(mutableListOf(Result.Failure(AnyError.ANY)))

        sut.loadPhotos()
        advanceUntilIdle()
        assertEquals("Error occurred, please try again.", sut.errorMessage)

        sut.resetErrorMessage()
        assertNull(sut.errorMessage)
    }

    @Test
    fun `load photos delivers empty photos when received empty photos from loader`() = runTest {
        val (sut, _) = makeSUT(mutableListOf(Result.Success(listOf())))

        sut.loadPhotos()
        assertTrue(sut.paginatedPhotos.value.isEmpty())

        advanceUntilIdle()
        assertTrue(sut.paginatedPhotos.value.isEmpty())
    }

    @Test
    fun `load photos delivers photos when received photos from loader`() = runTest {
        val photos = listOf(makePhoto(0), makePhoto(1), makePhoto(2))
        val (sut, _) = makeSUT(mutableListOf(Result.Success(photos)))

        sut.loadPhotos()
        assertTrue(sut.paginatedPhotos.value.isEmpty())

        advanceUntilIdle()
        assertEquals(photos, sut.paginatedPhotos.value)
    }

    @Test
    fun `load photos does not change previously loaded photos after an error from loader`() =
        runTest {
            val photos = listOf(makePhoto(0))
            val (sut, _) = makeSUT(
                mutableListOf(
                    Result.Success(photos),
                    Result.Failure(AnyError.ANY)
                )
            )

            sut.loadPhotos()
            advanceUntilIdle()
            assertEquals(photos, sut.paginatedPhotos.value)

            sut.loadPhotos()
            advanceUntilIdle()
            assertEquals(photos, sut.paginatedPhotos.value)
        }

    @Test
    fun `loadPhotos does not delivers loadMore on loader error`() = runTest {
        val (sut, _) = makeSUT(mutableListOf(Result.Failure(AnyError.ANY)))

        sut.loadPhotos()
        advanceUntilIdle()

        assertNull(sut.paginatedPhotos.loadMore)
    }

    @Test
    fun `loadPhotos does not delivers loadMore when received empty photos from loader`() = runTest {
        val firstPhotos = listOf(makePhoto(0))
        val lastEmptyPhoto = listOf<Photo>()
        val (sut, _) = makeSUT(
            mutableListOf(
                Result.Success(firstPhotos),
                Result.Success(lastEmptyPhoto)
            )
        )

        sut.loadPhotos()
        advanceUntilIdle()

        val loadMore = sut.paginatedPhotos.loadMore
        loadMore?.invoke()
        advanceUntilIdle()

        assertNull(sut.paginatedPhotos.loadMore)
    }

    @Test
    fun `loadPhotos delivers loadMore when received photos from loader`() = runTest {
        val firstPhotos = listOf(makePhoto(0))
        val lastPhotos = listOf(makePhoto(1))
        val (sut, loader) = makeSUT(
            mutableListOf(
                Result.Success(firstPhotos),
                Result.Success(lastPhotos)
            )
        )

        sut.loadPhotos()
        advanceUntilIdle()

        val loadMore = sut.paginatedPhotos.loadMore
        assertNotNull(loadMore)
        assertEquals(firstPhotos, sut.paginatedPhotos.value)

        loadMore?.invoke()
        advanceUntilIdle()

        assertEquals(listOf(1, 2), loader.loggedPages)
        assertEquals(firstPhotos + lastPhotos, sut.paginatedPhotos.value)
    }

    @Test
    fun `reloads photos delivers initial photos from loader`() = runTest {
        val initialPhotos = listOf(makePhoto(0))
        val additionalPhotos = listOf(makePhoto(1))
        val (sut, _) = makeSUT(
            mutableListOf(
                Result.Success(initialPhotos),
                Result.Success(additionalPhotos),
                Result.Success(initialPhotos)
            )
        )

        sut.loadPhotos()
        advanceUntilIdle()
        sut.paginatedPhotos.loadMore?.invoke()
        advanceUntilIdle()

        assertEquals(initialPhotos + additionalPhotos, sut.paginatedPhotos.value)

        sut.loadPhotos()
        advanceUntilIdle()

        assertEquals(initialPhotos, sut.paginatedPhotos.value)
    }

    // region Helpers
    private fun makeSUT(stubs: MutableList<Result<List<Photo>, Error>> = mutableListOf()): Pair<PhotosViewModel, PhotosLoaderSpy> {
        val photosLoader = PhotosLoaderSpy(stubs)
        val sut = PhotosViewModel(photosLoader)
        return Pair(sut, photosLoader)
    }

    private class PhotosLoaderSpy(private val stubs: MutableList<Result<List<Photo>, Error>>) :
        PageablePhotosLoader {
        val loggedPages = mutableListOf<Int>()

        override suspend fun loadPhotos(page: Int): Result<List<Photo>, Error> {
            loggedPages.add(page)
            return stubs.removeFirst()
        }
    }
    // endregion
}