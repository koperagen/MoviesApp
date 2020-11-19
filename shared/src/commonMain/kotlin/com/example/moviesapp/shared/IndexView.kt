package com.example.moviesapp.shared

import com.arkivanov.mvikotlin.core.view.MviView
import com.example.moviesapp.shared.IndexView.Event
import com.example.moviesapp.shared.IndexView.Model
import com.example.moviesapp.shared.network.Movie

interface IndexView : MviView<Model, Event> {
    sealed class Event {
        object NextPageClick : Event()
        object PreviousPageClick : Event()
        data class MovieClick(val movie: Movie) : Event()
    }

    data class Model(
        val page: Int,
        val movies: List<FavouriteMovie>,
        val hasNextPage: Boolean,
        val hasPreviousPage: Boolean
    )
}