package com.example.moviesapp.shared

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.example.moviesapp.shared.IndexStore.Intent
import com.example.moviesapp.shared.IndexStore.News
import com.example.moviesapp.shared.IndexStore.State
import com.example.moviesapp.shared.network.Movie
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

internal interface IndexStore : Store<Intent, State, News> {

    sealed class Intent {
        data class SetList(val movies: List<Int>) : Intent()
        data class ToggleMovie(val movie: Int) : Intent()
        object NextPage : Intent()
        object PreviousPage : Intent()
    }

    data class State(
        val currentPage: Int,
        val totalPages: Int,
        val movies: List<FavouriteMovie>,
    )

    sealed class News {
        object NoMorePages : News()
    }

}

data class FavouriteMovie(val movie: Movie, val isFavourite: Boolean)

internal class IndexStoreFactory(
    private val factory: StoreFactory,
    private val favouritesStore: FavouritesStore,
    private val movieStore: MovieStore,
    private val mainContext: CoroutineContext
) {

    fun create(): IndexStore = object : IndexStore, Store<Intent, State, News> by factory.create(
        name = "IndexStore",
        initialState = State(0, 0, emptyList()),
        bootstrapper = Bootstrapper(),
        executorFactory = ::ExecutorImpl,
        reducer = ReducerImpl,
    ) {}

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State {
            return when (result) {
                is Result.UpdatedList -> copy(movies = result.movies)
            }
        }
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Action, State, Result, News>(mainContext) {
        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            return when (intent) {
                is Intent.SetList -> favouritesStore.accept(FavouritesStore.Intent.SetList(intent.movies))
                is Intent.ToggleMovie -> favouritesStore.accept(FavouritesStore.Intent.ToggleMovie(intent.movie))
                is Intent.NextPage -> movieStore.accept(MovieStore.Intent.NextPage)
                is Intent.PreviousPage -> movieStore.accept(MovieStore.Intent.PreviousPage)
            }
        }

        override suspend fun executeAction(action: Action, getState: () -> State) {
            return when (action) {
                is Action.Combine -> combine(action.movies.movies, action.favourites.movies)
                is Action.PropagateNews.MovieNews -> publish(map(action))
            }
        }

        private fun combine(movies: List<Movie>, favourites: List<Int>) {
            val favourites = favourites.toSet()
            val favouriteMovies = movies
                .map { movie ->
                    val id = movie.id
                    FavouriteMovie(movie, id in favourites)
                }
            dispatch(Result.UpdatedList(favouriteMovies))
        }

        private suspend fun map(action: Action.PropagateNews.MovieNews): News {
            return when (action.news) {
                MovieStore.News.NoMorePages -> News.NoMorePages
            }
        }

    }

    private inner class Bootstrapper : SuspendBootstrapper<Action>(), CoroutineScope by CoroutineScope(mainContext) {

        override suspend fun bootstrap() {
            combine(favouritesStore.states, movieStore.states) { favourites, movies ->
                Action.Combine(favourites, movies)
            }
                .onEach { dispatch(it) }
                .launchIn(this)

            movieStore.labels
                .onEach { dispatch(Action.PropagateNews.MovieNews(it)) }
                .launchIn(this)
        }

        override fun dispose() {
            super.dispose()
            cancel()
        }

    }

    private sealed class Result {
        data class UpdatedList(val movies: List<FavouriteMovie>) : Result()
    }

    private sealed class Action {
        data class Combine(val favourites: FavouritesStore.State, val movies: MovieStore.State): Action()
        sealed class PropagateNews : Action() {
            data class MovieNews(val news: MovieStore.News) : PropagateNews()
        }
    }

}