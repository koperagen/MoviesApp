package com.example.moviesapp.shared

import com.example.moviesapp.shared.cache.AppDatabase
import com.example.moviesapp.shared.cache.FavouriteMovies
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

interface FavouritesDatabase {
    suspend fun addFavourite(id: Int)
    suspend fun removeFavourite(id: Int)
    fun getFavourites(): Flow<List<Int>>
}

internal class FavouriteDatabaseImpl(
    private val database: AppDatabase,
    private val ioContext: CoroutineContext
) : FavouritesDatabase {

    override suspend fun addFavourite(id: Int) {
        database.appDatabaseQueries.addFavourite(FavouriteMovies(id))
    }

    override suspend fun removeFavourite(id: Int) {
        database.appDatabaseQueries.removeFavourite(id)
    }

    override fun getFavourites(): Flow<List<Int>> = database.appDatabaseQueries
        .allFavourites()
        .asFlow()
        .mapToList(ioContext)

}