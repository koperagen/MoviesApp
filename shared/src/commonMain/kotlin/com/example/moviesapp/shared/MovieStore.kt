package com.example.moviesapp.shared

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.example.moviesapp.shared.MovieStore.Intent
import com.example.moviesapp.shared.MovieStore.News
import com.example.moviesapp.shared.MovieStore.State
import com.example.moviesapp.shared.network.Movie
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal interface MovieStore : Store<Intent, State, News> {
    sealed class Intent {
        object NextPage : Intent()
        object PreviousPage : Intent()
    }

    data class State(val currentPage: Int, val totalPages: Int, val movies: List<Movie>)

    sealed class News {
        object NoMorePages : News()
    }
}


internal class MovieStoreFactory(
    private val factory: StoreFactory,
    private val repository: MoviesRepository,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext
) {

    fun create(): MovieStore = object : MovieStore, Store<Intent, State, News> by factory.create(
        name = "MovieStore",
        initialState = State(0, 0, emptyList()),
        bootstrapper = SimpleBootstrapper(Action.LoadFirstPage),
        executorFactory = ::ExecutorImpl,
        reducer = ReducerImpl
    ) {}

    private sealed class Result {
        data class PageLoaded(val page: Int, val totalPages: Int, val movies: List<Movie>) : Result()
    }

    private sealed class Action {
        object LoadFirstPage : Action()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Action, State, Result, News>(mainContext) {

        override suspend fun executeAction(action: Action, getState: () -> State) {
            return when (action) {
                Action.LoadFirstPage -> loadPage(1)
            }
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            val state = getState()
            return when (intent) {
                Intent.NextPage -> {
                    loadPage(state.currentPage + 1)
                }
                Intent.PreviousPage -> {
                    if (state.currentPage < 1) {
                        publish(News.NoMorePages)
                    } else {
                        loadPage(state.currentPage - 1)
                    }
                }
            }
        }

        private suspend fun loadPage(page: Int) {
            val movies = withContext(ioContext) { repository.getMovies(page) }
            dispatch(Result.PageLoaded(
                page = page,
                totalPages = movies.totalPages,
                movies = movies.results
            ))
        }

    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State {
            return when (result) {
                is Result.PageLoaded -> copy(movies = result.movies, currentPage = result.page, totalPages = totalPages)
            }
        }
    }

}