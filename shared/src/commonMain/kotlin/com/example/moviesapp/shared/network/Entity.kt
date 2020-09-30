package com.example.moviesapp.shared.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    @SerialName("poster_path") val posterPath: String?,
    val adult: Boolean,
    val overview: String,
    @SerialName("release_date") val releaseDate: String,
    @SerialName("genre_ids") val genreIds: List<Int>,
    val id: Int,
    @SerialName("original_title") val originalTitle: String,
    @SerialName("original_language") val originalLanguage: String,
    val title: String,
    @SerialName("backdrop_path") val backdropPath: String?,
    val popularity: Double,
    @SerialName("vote_count") val voteCount: Int,
    val video: Boolean,
    @SerialName("vote_average") val voteAverage: Double,
)

@Serializable
data class MovieDiscovery(
    val page: Int,
    val results: List<Movie>,
    @SerialName("total_results") val totalResults: Int,
    @SerialName("total_pages") val totalPages: Int,
)