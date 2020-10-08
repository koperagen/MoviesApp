package com.example.moviesapp.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.parameter


public interface MovieApi {
    suspend fun getAllMovies(page: Int? = null): MovieDiscovery
}

@Suppress("FunctionName")
public fun MovieApi(apiKey: String): MovieApi = MovieApiImpl(apiKey)

internal class MovieApiImpl(private val apiKey: String) : MovieApi {
    private val client = HttpClient {
        Json {
            serializer = KotlinxSerializer()
        }
    }

    override suspend fun getAllMovies(page: Int?): MovieDiscovery {
        return client.get("https://api.themoviedb.org/3/discover/movie") {
            parameter("api_key", apiKey)
            parameter("language", "ru-RU")
            parameter("include_adult", false)
            parameter("include_video", false)
        }
    }
}