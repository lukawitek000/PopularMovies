package com.lukasz.witkowski.android.moviestemple.adapters


import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.lukasz.witkowski.android.moviestemple.adapters.MoviesAdapter.MoviesAdapterViewHolder
import com.lukasz.witkowski.android.moviestemple.R
import com.squareup.picasso.Picasso
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lukasz.witkowski.android.moviestemple.models.Movie

class MoviesAdapter(private val clickHandler: MovieAdapterOnClickHandler) : ListAdapter<Movie, MoviesAdapterViewHolder>(MoviesDiffCallback()) {

    interface MovieAdapterOnClickHandler {
        fun onClick(movie: Movie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesAdapterViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.movies_list_item, parent, false)
        return MoviesAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoviesAdapterViewHolder, position: Int) {
        val movie = getItem(position)

        if(movie.posterUri != null) {
            Picasso.with(holder.posterImage.context)
                    .load(movie.posterUri)
                    .into(holder.posterImage)
        }else{
            Picasso.with(holder.posterImage.context)
                    .load(R.drawable.default_movie_poster)
                    .into(holder.posterImage)
        }

    }


    inner class MoviesAdapterViewHolder(itemView: View) : ViewHolder(itemView), View.OnClickListener {
        val posterImage: ImageView = itemView.findViewById(R.id.poster_image_view)
        override fun onClick(view: View) {
            val adapterPosition = adapterPosition
            clickHandler.onClick(getItem(adapterPosition))
        }


        init {
            itemView.setOnClickListener(this)
        }
    }
}


class MoviesDiffCallback : DiffUtil.ItemCallback<Movie>(){
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem == newItem
    }
}