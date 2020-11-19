package com.example.moviesapp.androidApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.example.moviesapp.shared.FavouriteMovie
import com.example.moviesapp.shared.IndexController
import com.example.moviesapp.shared.IndexView
import com.example.moviesapp.shared.IndexView.Event
import com.example.moviesapp.shared.IndexView.Model
import com.example.moviesapp.shared.cache.DriverFactory
import com.example.moviesapp.shared.cache.createDatabase
import com.example.moviesapp.shared.network.Movie
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity() {

    private val controller = IndexController(
        dbFactory = { createDatabase(DriverFactory(this)) },
        defaultStoreFactory = LoggingStoreFactory(DefaultStoreFactory),
        apiKey = "5e2154d0d7039ef73d73e64af47e8e6e",
        mainContext = Dispatchers.Main,
        ioContext = Dispatchers.IO,
        lifecycle = lifecycle.asMviLifecycle()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val view = findViewById<View>(R.id.main_view)
        val movieView = IndexViewImpl(view)
        controller.onViewCreated(movieView, lifecycle.asMviLifecycle())
    }

}

class IndexViewImpl(private val root: View): BaseMviView<Model, Event>(), IndexView {

    private val adapter: MoviesAdapter = MoviesAdapter { dispatch(Event.MovieClick(it)) }
    private val movies: RecyclerView = root.findViewById(R.id.movies)
    private val nextPage: Button = root.findViewById(R.id.nextPage)
    private val previousPage: Button = root.findViewById(R.id.previousPage)

    init {
        movies.adapter = adapter
        nextPage.setOnClickListener {
            dispatch(Event.NextPageClick)
        }
        previousPage.setOnClickListener {
            dispatch(Event.PreviousPageClick)
        }
    }

    override fun render(model: Model) {
        adapter.submitList(model.movies)
    }

}

private class MoviesAdapter(private val onClick: (Movie) -> Unit) : ListAdapter<FavouriteMovie, MovieViewHolder>(MovieDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return MovieViewHolder.from(parent, onClick)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

private class MovieViewHolder private constructor(
    private val view: View,
    private val onClick: (Movie) -> Unit
) : RecyclerView.ViewHolder(view) {

    private val title: TextView = view.findViewById(R.id.movieTitle)
    private val voteAverage: TextView = view.findViewById(R.id.voteAverage)
    private val poster: ImageView = view.findViewById(R.id.poster)

    fun bind(item: FavouriteMovie) {
        val (movie, favourite) = item
        title.text = buildString(movie.title.length + 1) {
            if (favourite) append("*")
            append(movie.title)
        }
        voteAverage.text = view.context.getString(R.string.vote_average, movie.voteAverage)
        Picasso.get()
            .load("http://image.tmdb.org/t/p/w500" + movie.posterPath)
            .error(R.drawable.ic_baseline_build_24)
            .into(poster)
        view.setOnClickListener {
            onClick(movie)
        }
    }

    companion object {
        fun from(parent: ViewGroup, onClick: (Movie) -> Unit): MovieViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.movie_item, parent, false)
            return MovieViewHolder(view, onClick)
        }
    }
}

private object MovieDiffCallback : DiffUtil.ItemCallback<FavouriteMovie>() {

    override fun areItemsTheSame(oldItem: FavouriteMovie, newItem: FavouriteMovie): Boolean {
        return oldItem.movie.id == newItem.movie.id
    }

    override fun areContentsTheSame(oldItem: FavouriteMovie, newItem: FavouriteMovie): Boolean {
        return oldItem == newItem
    }

}