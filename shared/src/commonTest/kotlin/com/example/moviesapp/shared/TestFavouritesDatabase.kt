package com.example.moviesapp.shared

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow


class TestFavouritesDatabase : FavouritesDatabase {

    private val _favourites = MutableStateFlow(emptyList<Int>())

    var favouritesList: List<Int>
        set(value) {
            _favourites.value = value
        }
        get() = _favourites.value


    override suspend fun addFavourite(id: Int) {
        favouritesList = favouritesList + id
    }

    override suspend fun removeFavourite(id: Int) {
        favouritesList = favouritesList - id
    }

    override val favourites: Flow<List<Int>> = _favourites

}