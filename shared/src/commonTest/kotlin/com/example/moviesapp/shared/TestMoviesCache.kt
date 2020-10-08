package com.example.moviesapp.shared

import com.example.moviesapp.shared.cache.MoviesCache
import com.example.moviesapp.shared.network.MovieDiscovery

internal class TestMoviesCache : MoviesCache() {

    private val cache: MutableMap<Int, MovieDiscovery> = mutableMapOf()

    override fun getAllMovies(page: Int): MovieDiscovery? {
        return cache[page]
    }

    override fun persist(movies: MovieDiscovery) {
        cache[movies.page] = movies
    }

}