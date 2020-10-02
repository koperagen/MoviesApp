package com.example.moviesapp.shared.cache

import com.example.moviesapp.shared.network.Movie
import com.example.moviesapp.shared.network.MovieDiscovery
import com.squareup.sqldelight.ColumnAdapter

internal fun createDatabase(driverFactory: DriverFactory): AppDatabase =
    AppDatabase(
        driver = driverFactory.create(),
        MovieDiscoveriesAdapter = MovieDiscoveries.Adapter(IntListToStringAdapter),
        MoviesAdapter = Movies.Adapter(IntListToStringAdapter)
    )

internal class MoviesCache(private val database: AppDatabase) {

    fun getAllMovies(page: Int): MovieDiscovery? {
        return database.appDatabaseQueries.loadDiscovery(page) { page, moviesIds, total_results, total_pages ->
            val movies = database.appDatabaseQueries
                .selectMoviesByIds(moviesIds)
                .executeAsList()
                .map { it.toEntity() }
            MovieDiscovery(page, movies, total_results, total_pages)
        }.executeAsOneOrNull()
    }

    fun cache(movies: MovieDiscovery) {
        movies.results.forEach {
            cacheMovie(it)
        }
        database.appDatabaseQueries.saveDiscovery(
            MovieDiscoveries(
                page = movies.page,
                movies = movies.results.map { it.id },
                total_pages = movies.totalPages,
                total_results = movies.totalResults
            )
        )
    }

    private fun cacheMovie(movie: Movie) {
        database.appDatabaseQueries.saveMovie(movie.toDto())
    }

    private fun Movies.toEntity() = Movie(
        posterPath = poster_path,
        adult = adult,
        overview = overview,
        releaseDate = release_date,
        genreIds = genre_ids,
        id = id,
        originalTitle = original_title,
        originalLanguage = original_language,
        title = title,
        backdropPath = backdrop_path,
        popularity = popularity,
        voteCount = vote_count,
        video = video,
        voteAverage = vote_average
    )

    private fun Movie.toDto() = Movies(
        poster_path = posterPath,
        adult = adult,
        overview = overview,
        release_date = releaseDate,
        genre_ids = genreIds,
        id = id,
        original_title = originalTitle,
        original_language = originalLanguage,
        title = title,
        backdrop_path = backdropPath,
        popularity = popularity,
        vote_count = voteCount,
        video = video,
        vote_average = voteAverage
    )
}

private object IntListToStringAdapter : ColumnAdapter<List<Int>, String> {
    override fun decode(databaseValue: String): List<Int> {
        return databaseValue.split(" ").map { it.toInt() }
    }

    override fun encode(value: List<Int>): String {
        return value.joinToString(" ")
    }
}