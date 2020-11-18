package com.example.moviesapp.shared

import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FavouritesStoreTest {

    private lateinit var database: TestFavouritesDatabase
    private lateinit var store: FavouritesStore

    @BeforeTest
    fun before() {
        isAssertOnMainThreadEnabled = false
    }

    @AfterTest
    fun after() {
        isAssertOnMainThreadEnabled = true
    }

    @Test
    fun adds_movie_to_favourite_ON_Intent_ToggleMovie_WHEN_movie_not_favourite() {
        createStore()

        store.accept(FavouritesStore.Intent.ToggleMovie(1))

        assertEquals(listOf(1), store.state.movies)
    }

    @Test
    fun saves_favourite_to_database_ON_Intent_ToggleMovie_WHEN_movie_not_favourite() {
        createStore()

        store.accept(FavouritesStore.Intent.ToggleMovie(1))

        assertEquals(listOf(1), database.favouritesList)
    }

    @Test
    fun state_is_upToDate_with_database() {
        createStore()
        database.favouritesList = listOf(1, 2, 3)

        assertEquals(listOf(1, 2, 3), store.state.movies)
    }

    @Test
    fun removes_movie_from_favourite_ON_Intent_ToggleMovie_WHEN_movie_is_favourite() {
        createStore()
        database.favouritesList = listOf(1, 2, 3)

        store.accept(FavouritesStore.Intent.ToggleMovie(2))

        assertEquals(listOf(1, 3), store.state.movies)
    }

    @Test
    fun removes_favourite_from_database_ON_Intent_ToggleMovie_WHEN_movie_is_favourite() {
        createStore()
        database.favouritesList = listOf(1, 2, 3)

        store.accept(FavouritesStore.Intent.ToggleMovie(2))

        assertEquals(listOf(1, 3), database.favouritesList)
    }

    private fun createStore() {
        database = TestFavouritesDatabase()
        store = FavouritesStoreFactory(
            DefaultStoreFactory,
            Dispatchers.Unconfined,
            Dispatchers.Unconfined,
            database,
        ).create()
    }

}