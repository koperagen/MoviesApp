package com.example.moviesapp.shared

import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MovieStoreTest {

    private lateinit var store: MovieStore

    @BeforeTest
    fun before() {
        isAssertOnMainThreadEnabled = false
    }

    @AfterTest
    fun after() {
        isAssertOnMainThreadEnabled = true
    }

    @Test
    fun shows_first_page() {
        createStore()

        assertEquals(1, store.state.currentPage)
    }

    @Test
    fun loads_next_page_WHEN_Intent_NextPage() {
        createStore()

        store.accept(MovieStore.Intent.NextPage)

        assertEquals(2, store.state.currentPage)
    }

    private fun createStore() {
        store = MovieStoreFactory(
            DefaultStoreFactory,
            TestMovieApi(),
            Dispatchers.Unconfined,
            Dispatchers.Unconfined
        ).create()
    }

}