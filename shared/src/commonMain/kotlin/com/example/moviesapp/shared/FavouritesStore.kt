package com.example.moviesapp.shared

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.example.moviesapp.shared.FavouritesStore.Intent
import com.example.moviesapp.shared.FavouritesStore.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal interface FavouritesStore : Store<Intent, State, Nothing> {
    sealed class Intent {
        data class AddMovie(val movieId: Int) : Intent()
        data class RemoveMovie(val movieId: Int) : Intent()
        data class SetList(val movies: List<Int>) : Intent()
    }

    data class State(val movies: List<Int>)
}

internal class FavouritesStoreFactory(
    private val factory: StoreFactory,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext,
    private val database: FavouritesDatabase
) {

    fun create(): FavouritesStore = object : FavouritesStore, Store<Intent, State, Nothing> by factory.create(
        name = "FavouritesStore",
        initialState = State(emptyList()),
        executorFactory = ::ExecutorImpl,
        bootstrapper = BootstrapperImpl(),
        reducer = ReducerImpl
    ) {}

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State {
            return when (result) {
                is Result.UpdatedList -> copy(movies = result.movies)
            }
        }
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Action, State, Result, Nothing>(mainContext) {

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            return when (intent) {
                is Intent.AddMovie -> addFavourite(getState(), intent.movieId)
                is Intent.RemoveMovie -> removeFavourite(getState(), intent.movieId)
                is Intent.SetList -> dispatch(Result.UpdatedList(intent.movies))
            }
        }

        override suspend fun executeAction(action: Action, getState: () -> State) {
            return when (action) {
                is Action.UpdateFavourites -> dispatch(Result.UpdatedList(action.movies))
            }
        }

        private suspend fun addFavourite(state: State, id: Int) {
            withContext(ioContext) {
                database.addFavourite(id)
            }
            dispatch(Result.UpdatedList(movies = state.movies + id))
        }

        private suspend fun removeFavourite(state: State, id: Int) {
            withContext(ioContext) {
                database.removeFavourite(id)
            }
            dispatch(Result.UpdatedList(movies = state.movies - id))
        }
    }

    private inner class BootstrapperImpl : SuspendBootstrapper<Action>(mainContext) {
        private val scope = CoroutineScope(ioContext)

        override suspend fun bootstrap() {
            database.favourites
                .onEach { dispatch(Action.UpdateFavourites(movies = it)) }
                .flowOn(mainContext)
                .launchIn(scope)
        }

        override fun dispose() {
            super.dispose()
            scope.cancel()
        }
    }

    private sealed class Result {
        data class UpdatedList(val movies: List<Int>) : Result()
    }

    private sealed class Action {
        data class UpdateFavourites(val movies: List<Int>) : Action()
    }

}