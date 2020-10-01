package com.example.moviesapp.shared

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.example.moviesapp.shared.MovieStore.Intent
import com.example.moviesapp.shared.MovieStore.State
import com.example.moviesapp.shared.network.Movie
import com.example.moviesapp.shared.network.MovieApi
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal interface MovieStore : Store<Intent, State, Nothing> {
    sealed class Intent {
        object NextPage : Intent()
    }

    data class State(val movies: List<Movie>, val currentPage: Int)
}

internal class MovieStoreFactory(
    private val factory: StoreFactory,
    private val api: MovieApi,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext
) {

    fun create(): MovieStore = object : MovieStore, Store<Intent, State, Nothing> by factory.create(
        name = "MovieStore",
        initialState = State(emptyList(), 0),
        bootstrapper = SimpleBootstrapper(Action.LoadFirstPage),
        executorFactory = ::ExecutorImpl,
        reducer = ReducerImpl
    ) {}

    private sealed class Result {
        data class PageLoaded(val page: Int, val movies: List<Movie>, val totalPages: Int) : Result()
    }

    private sealed class Action {
        object LoadFirstPage : Action()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Action, State, Result, Nothing>(mainContext) {

        override suspend fun executeAction(action: Action, getState: () -> State) {
            return when (action) {
                Action.LoadFirstPage -> loadPage(1)
            }
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            return when (intent) {
                Intent.NextPage -> loadPage(getState().currentPage + 1)
            }
        }

        private suspend fun loadPage(page: Int) {
            withContext(ioContext) {
                val movies = api.getAllMovies(page)
                dispatch(Result.PageLoaded(page = page, movies = movies.results, totalPages = movies.totalPages))
            }
        }

    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State {
            return when (result) {
                is Result.PageLoaded -> copy(movies = result.movies, currentPage = result.page)
            }
        }
    }

}