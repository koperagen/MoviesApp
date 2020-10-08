package com.example.moviesapp.shared

import com.arkivanov.mvikotlin.core.view.MviView
import com.example.moviesapp.shared.MovieView.Event
import com.example.moviesapp.shared.MovieView.Model
import com.example.moviesapp.shared.network.Movie

interface MovieView : MviView<Model, Event> {
    sealed class Event {
        object NextPageClick : Event()
    }

    data class Model(
        val page: Int,
        val movies: List<Movie>,
        val hasNextPage: Boolean,
        val hasPreviousPage: Boolean
    )
}