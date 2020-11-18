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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

class MovieController(
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
    private var binder: Binder? = null

    init {
        lifecycle.doOnDestroy(store::dispose)
    }

    fun onViewCreated(view: MovieView, viewLifecycle: Lifecycle) {
        binder = bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            store.states.map(stateToModel) bindTo view
            view.events.map(eventToMovieIntent).filterNotNull() bindTo store
            view.events.map(eventToFavouritesIntent).filterNotNull() bindTo favouritesStore
        }
    }

}

private val eventToMovieIntent: suspend MovieView.Event.() -> MovieStore.Intent? = {
    when (this) {
        MovieView.Event.NextPageClick -> MovieStore.Intent.NextPage
        MovieView.Event.PreviousPageClick -> MovieStore.Intent.NextPage
        is MovieView.Event.MovieClick -> null
    }
}

private val eventToFavouritesIntent: suspend MovieView.Event.() -> FavouritesStore.Intent? = {
    when (this) {
        MovieView.Event.NextPageClick ->  null
        MovieView.Event.PreviousPageClick -> null
        is MovieView.Event.MovieClick -> FavouritesStore.Intent.ToggleMovie(movie.id)
    }
}

private val stateToModel: suspend MovieStore.State.() -> MovieView.Model = {
    MovieView.Model(currentPage, movies, currentPage > 1, currentPage != totalPages)
}