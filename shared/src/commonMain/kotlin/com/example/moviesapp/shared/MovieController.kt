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
    private var binder: Binder? = null

    init {
        lifecycle.doOnDestroy(store::dispose)
    }

    fun onViewCreated(view: MovieView, viewLifecycle: Lifecycle) {
        binder = bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            store.states.map(stateToModel) bindTo view
            view.events.map(eventToIntent) bindTo store
        }
    }

}

private val eventToIntent: suspend MovieView.Event.() -> MovieStore.Intent = {
    when (this) {
        MovieView.Event.NextPageClick -> MovieStore.Intent.NextPage
    }
}

private val stateToModel: suspend MovieStore.State.() -> MovieView.Model = {
    MovieView.Model(currentPage, movies, currentPage > 1, currentPage != totalPages)
}