package com.example.moviesapp.shared

import com.example.moviesapp.shared.cache.MoviesCache
import com.example.moviesapp.shared.network.MovieApi
import com.example.moviesapp.shared.network.MovieDiscovery

internal interface MoviesRepository {
    suspend fun getMovies(page: Int): MovieDiscovery
}

internal class MovieRepositoryImpl(
    private val network: MovieApi,
    private val cache: MoviesCache,
) : MoviesRepository {
    override suspend fun getMovies(page: Int): MovieDiscovery {
        return cache.getAllMovies(page)
            ?: network.getAllMovies(page).also { cache.persist(it) }
    }
}