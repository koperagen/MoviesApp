package com.example.moviesapp.shared

import com.example.moviesapp.shared.network.Movie
import com.example.moviesapp.shared.network.MovieApi
import com.example.moviesapp.shared.network.MovieDiscovery

class TestMovieApi : MovieApi {

    override suspend fun getAllMovies(page: Int?): MovieDiscovery {
        val actualPage = page ?: 1
        val offset = (actualPage - 1) * PAGE_SIZE
        return MovieDiscovery(
            page = page ?: 1,
            results = List(PAGE_SIZE) { createMovie(offset + it) },
            totalPages = TOTAL_PAGES,
            totalResults = TOTAL_RESULTS
        )
    }

    private fun createMovie(id: Int): Movie {
        return Movie(
            posterPath = null,
            adult = false,
            overview = "Empty",
            releaseDate = "",
            genreIds = emptyList(),
            id = id,
            originalTitle = "Movie $id",
            originalLanguage = "",
            title = "",
            backdropPath = "",
            popularity = 5.0,
            voteCount = 100,
            video = false,
            voteAverage = 8.0
        )
    }

    private companion object {
        private const val PAGE_SIZE = 50
        private const val TOTAL_PAGES = 25
        private const val TOTAL_RESULTS = PAGE_SIZE * TOTAL_PAGES
    }
}