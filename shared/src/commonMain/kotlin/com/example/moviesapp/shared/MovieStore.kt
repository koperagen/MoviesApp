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

    data class State(val currentPage: Page?, val totalPages: Int, val movies: List<Movie>)

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
        initialState = State(null, 0, emptyList()),
        bootstrapper = SimpleBootstrapper(Action.LoadFirstPage),
        executorFactory = ::ExecutorImpl,
        reducer = ReducerImpl
    ) {}

    private sealed class Result {
        data class PageLoaded(val page: Page, val totalPages: Int, val movies: List<Movie>) : Result()
    }

    private sealed class Action {
        object LoadFirstPage : Action()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Action, State, Result, News>(mainContext) {

        override suspend fun executeAction(action: Action, getState: () -> State) {
            return when (action) {
                Action.LoadFirstPage -> loadPage(Page.FIRST)
            }
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            val state = getState()
            return when (intent) {
                Intent.NextPage -> {
                    state.currentPage.next(state.totalPages)
                        ?.let { loadPage(it) }
                        ?: publish(News.NoMorePages)
                }
                Intent.PreviousPage -> {
                    state.currentPage?.prev()
                        ?.let { loadPage(it) }
                        ?: publish(News.NoMorePages)
                }
            }
        }

        private suspend fun loadPage(page: Page) {
            val movies = withContext(ioContext) { repository.getMovies(page.value) }
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