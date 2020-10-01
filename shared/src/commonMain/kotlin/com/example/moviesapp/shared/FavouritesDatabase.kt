package com.example.moviesapp.shared

import kotlinx.coroutines.flow.Flow

interface FavouritesDatabase {
    suspend fun addFavourite(id: Int)
    suspend fun removeFavourite(id: Int)
    val favourites: Flow<List<Int>>
}