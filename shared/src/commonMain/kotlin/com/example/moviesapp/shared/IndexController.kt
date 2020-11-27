package com.example.moviesapp.shared

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.example.moviesapp.shared.cache.AppDatabase
import com.example.moviesapp.shared.cache.DatabaseMoviesCache
import com.example.moviesapp.shared.network.MovieApi
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

class IndexController(
    private val dbFactory: () -> AppDatabase,
    private val defaultStoreFactory: StoreFactory,
    private val apiKey: String,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext,
    private val lifecycle: Lifecycle
) {
    private val store: MovieStore = MovieStoreFactory(
        defaultStoreFactory,
        MovieRepositoryImpl(
            cache = DatabaseMoviesCache(dbFactory()),
            network = MovieApi(apiKey)
        ),
        mainContext = mainContext,
        ioContext = ioContext
    ).create()

    private val favouritesStore: FavouritesStore = FavouritesStoreFactory(
        defaultStoreFactory,
        mainContext,
        ioContext,
        FavouriteDatabaseImpl(dbFactory(), ioContext)
    ).create()

    private val indexStore: IndexStore = IndexStoreFactory(
        defaultStoreFactory, favouritesStore, store, mainContext
    ).create()

    private var binder: Binder? = null

    init {
        lifecycle.doOnDestroy(store::dispose)
    }

    fun onViewCreated(view: IndexView, viewLifecycle: Lifecycle) {
        binder = bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            indexStore.states.map(stateToModel) bindTo view
            view.events.map(eventToMovieIntent) bindTo indexStore
        }
    }

}

private val eventToMovieIntent: suspend IndexView.Event.() -> IndexStore.Intent = {
    when (this) {
        is IndexView.Event.NextPageClick -> IndexStore.Intent.NextPage
        is IndexView.Event.PreviousPageClick -> IndexStore.Intent.PreviousPage
        is IndexView.Event.MovieClick -> IndexStore.Intent.ToggleMovie(movie.id)
    }
}

private val stateToModel: suspend IndexStore.State.() -> IndexView.Model = {
    IndexView.Model(currentPage, movies, hasMore(), currentPage > 1)
}

private fun IndexStore.State.hasMore() = currentPage < totalPages