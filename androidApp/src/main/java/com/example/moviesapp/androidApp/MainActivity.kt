package com.example.moviesapp.androidApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.moviesapp.shared.Greeting
import com.example.moviesapp.shared.MovieController
import com.example.moviesapp.shared.MovieView
import com.example.moviesapp.shared.MovieView.Event
import com.example.moviesapp.shared.MovieView.Model
import com.example.moviesapp.shared.cache.DriverFactory
import com.example.moviesapp.shared.cache.createDatabase
import com.example.moviesapp.shared.network.Movie
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity() {

    private val controller = MovieController(
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
        val movieView = MovieViewImpl(view)
        controller.onViewCreated(movieView, lifecycle.asMviLifecycle())
    }

}

class MovieViewImpl(private val root: View): BaseMviView<Model, Event>(), MovieView {

    private val adapter: MoviesAdapter = MoviesAdapter()
    private val movies: RecyclerView = root.findViewById(R.id.movies)

    init {
        movies.adapter = adapter
    }

    override fun render(model: Model) {
        adapter.submitList(model.movies)
    }

}

private class MoviesAdapter : ListAdapter<Movie, MovieViewHolder>(MovieDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return MovieViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

private class MovieViewHolder private constructor(private val view: View) : RecyclerView.ViewHolder(view) {

    private val title: TextView = view.findViewById(R.id.movieTitle)
    private val voteAverage: TextView = view.findViewById(R.id.voteAverage)
    private val poster: ImageView = view.findViewById(R.id.poster)

    fun bind(item: Movie) {
        title.text = item.title
        voteAverage.text = view.context.getString(R.string.vote_average, item.voteAverage)
        Picasso.get()
            .load("http://image.tmdb.org/t/p/w500" + item.posterPath)
            .error(R.drawable.ic_baseline_build_24)
            .into(poster)
    }

    companion object {
        fun from(parent: ViewGroup): MovieViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.movie_item, parent, false)
            return MovieViewHolder(view)
        }
    }
}

private object MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {

    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem == newItem
    }

}